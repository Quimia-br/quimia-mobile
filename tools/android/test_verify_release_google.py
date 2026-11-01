import unittest
from verify_release_google import verify_google_resources


def dump_value(name, value):
    return f'        resource 0x7f11004d com.quimia.android:string/{name}: t=0x03\n            (string8) "{value}"\n'


class VerifyReleaseGoogleTest(unittest.TestCase):
    def setUp(self):
        self.firebase = dump_value("google_app_id", "1:test:android:test") + dump_value("google_api_key", "test-key")

    def test_accepts_complete_apk(self):
        verify_google_resources(self.firebase + dump_value("default_web_client_id", "test.apps.googleusercontent.com"))

    def test_rejects_removed_oauth_resource_even_when_firebase_survives(self):
        with self.assertRaisesRegex(ValueError, "default_web_client_id"):
            verify_google_resources(self.firebase)

    def test_spec_without_actual_string_is_not_enough(self):
        with self.assertRaises(ValueError):
            verify_google_resources(self.firebase + 'spec resource 0x7f11004d com.quimia.android:string/default_web_client_id: flags=0\n')

    def test_empty_and_invalid_configuration_fail(self):
        for client in ("", "not-an-oauth-client"):
            with self.subTest(client=client), self.assertRaises(ValueError):
                verify_google_resources(self.firebase + dump_value("default_web_client_id", client))


if __name__ == "__main__":
    unittest.main()
