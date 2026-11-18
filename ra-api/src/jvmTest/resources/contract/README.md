Schema snapshots of the live API responses, one `.schema` file per endpoint, used by `ApiContractTest`.

Generate them with a real account and commit the result:

    RA_USERNAME=… RA_API_KEY=… RA_UPDATE_SNAPSHOTS=true ./gradlew :ra-api:jvmContractTest

Or run the "API contract" workflow manually with "update snapshots" checked and download the `contract-snapshots` artifact.
