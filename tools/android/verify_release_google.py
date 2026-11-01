"""Check the Google configuration inside the final, resource-shrunk APK before publishing."""
import argparse
import re
import subprocess


def verify_google_resources(resource_dump: str) -> None:
    for name in ("default_web_client_id", "google_app_id", "google_api_key"):
        values = re.findall(
            rf"(?m)^\s*resource\s+0x[0-9a-f]+\s+[^\s]+:string/{name}:[^\n]*\n\s*\(string(?:8|16)\)\s+\"([^\"]*)\"",
            resource_dump,
        )
        if not values or not all(value.strip() for value in values):
            raise ValueError(f"Google configuration missing from final APK: {name}")
        if name == "default_web_client_id" and not all(value.endswith(".apps.googleusercontent.com") for value in values):
            raise ValueError("Invalid Google web OAuth client in final APK")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--aapt", required=True)
    parser.add_argument("--apk", required=True)
    args = parser.parse_args()
    result = subprocess.run([args.aapt, "dump", "--values", "resources", args.apk],
                            check=True, capture_output=True, text=True, encoding="utf-8")
    verify_google_resources(result.stdout)
    print("Final APK retains nonempty Google OAuth and Firebase configuration.")


if __name__ == "__main__":
    main()
