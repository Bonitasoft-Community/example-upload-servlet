# Examples of upload servlet usage

## Description of the example
This Groovy standalone application use Bonita Portal upload servlet and REST API to:
- Create a group in Bonita organization with an icon image
- Deploy a Bonita REST API extension

Upload servlet is used to upload the file (e.g. the image, the REST API extension zip file) and REST API call is used to register newly created item (e.g. the group, the REST API extension).

## Executing the example
To run this code:
- Clone the project
- run the command `./gradlew runScriptCreateGroup` or `./gradlew runScriptUploadRESTAPIextension`

## Compatibility
This project has been successfully tested on Bonita Community Edition 7.9.4. It should be compatible with any newer version and also with Enterprise Edition.

## Known limitations or issues
None so far. Please use GitHub issues tracker to report any bugs you may found.