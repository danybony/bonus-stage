# Releasing

Android releases are built and published by `.github/workflows/release.yml` when a `v*` tag is pushed. iOS releases go through TestFlight and need an Apple developer account (#18).

## One-time setup: the signing key

Every release must be signed with the same key, or users cannot update without uninstalling. Create it once, keep it outside the repository, and back it up (a password manager is a good place): if it is lost, existing installs can never be updated.

```sh
keytool -genkeypair -v -keystore bonus-stage-release.jks -alias bonus-stage \
  -keyalg RSA -keysize 4096 -validity 10000
```

Then add four repository secrets (Settings → Secrets and variables → Actions):

| Secret | Value |
| --- | --- |
| `BONUS_STAGE_KEYSTORE_BASE64` | `base64 -w0 bonus-stage-release.jks` (on macOS: `base64 -i bonus-stage-release.jks`) |
| `BONUS_STAGE_KEYSTORE_PASSWORD` | the keystore password |
| `BONUS_STAGE_KEY_ALIAS` | `bonus-stage` |
| `BONUS_STAGE_KEY_PASSWORD` | the key password |

For a signed build on your own machine, put the same names in `local.properties`, with `BONUS_STAGE_KEYSTORE` set to the path of the `.jks` file.

## Publishing a release

```sh
git checkout main && git pull
git tag v0.2.0          # or v0.3.0-beta.1 for a pre-release
git push origin v0.2.0
```

The workflow runs the tests, builds the signed APK (version name from the tag, version code from the workflow run number) and creates the GitHub release with notes generated from the merged pull requests. The APK and the R8 mapping file (to read stack traces from bug reports) are attached.

A debug build from a contributor's machine has a different signature: installing a release over it, or the other way round, needs an uninstall first, which deletes the app's local data.
