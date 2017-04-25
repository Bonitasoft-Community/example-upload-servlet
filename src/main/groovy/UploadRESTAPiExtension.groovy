#!/usr/bin/env groovy

import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.*

import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.ContentType


HTTPBuilder http = new HTTPBuilder('http://localhost:8080')

// Login
def bonitaToken = login(http)

// Upload REST API extension zip file from client to server temporary folder.
// Server answer with information about file location on server side.
def temporaryFileOnServer = uploadFile(http)

// Creation of REST API extension using the file previously upload in temporary folder
registerRestAPIExtension(http, temporaryFileOnServer, bonitaToken)

def login(HTTPBuilder http) {
	def bonitaToken
	
	http.request(POST) { multipartRequest ->
		// Login service URL
		uri.path = "/bonita/loginservice"

		// Content need to be URL encoded
		requestContentType = URLENC

		// User credentials
		body =  [username: 'walter.bates', password: 'bpm']

		// Parsing successful response to get the X-Bonita-API-Token value
		response.success = { resp, reader ->
			println "Login successful"
			
			// Iterate over all Set-Cookie headers
			resp.getHeaders('Set-Cookie').each {
				// Parse a Set-Cookie header to get an array with all the cookies 
				def currentHeaderCookies = it.value.split(";")

				// For each cookie of current Set-Cookie header
				currentHeaderCookies.each { cookie ->
					println cookie
					
					// Test if cookie is the X-Bonita-API-Token
					if(cookie.startsWith('X-Bonita-API-Token')) {
						// Parse the cookie to get the X-Bonita-API-Token value
						def tokenCookie = cookie.split('=')
						bonitaToken = tokenCookie[1]
						println "X-Bonita-API-Token value: ${bonitaToken}"
					}
				}
			}
		}
	}
	
	if(bonitaToken == null) {
		throw new Exception("Failed to login")
	} else {
		return bonitaToken
	}
}

def uploadFile(HTTPBuilder http) {
    // File upload using pageUpload servlet to a temporary folder
    def uploadResponse

    http.request(POST) { multipartRequest ->
        // URL of page upload servlet (REST API extension is handle as a page)
        uri.path = "/bonita/portal/pageUpload"

        // We use multipart content type to upload the file
        requestContentType = "multipart/form-data"

        // Create a multipart entity builder for our local test file (the SQL data source REST API extension)
        MultipartEntityBuilder multiPartContent = MultipartEntityBuilder.create().addBinaryBody("file", new FileInputStream("src/main/groovy/test.zip"), ContentType.create("application/zip"), "test.zip")

        multipartRequest.entity = multiPartContent.build()

        // Handle successful response
        response.success = { resp, reader ->
            if (resp.statusLine.statusCode == 200) {
                // Store reponse information that include information about temporary storage of the file on server side
                uploadResponse = reader.text
            } else {
                println resp.statusLine.statusCode
            }
        }
    }

    if(uploadResponse == null) {
        throw new Exception("Failed to upload the file")
    } else {
        return uploadResponse
    }
}

void registerRestAPIExtension(HTTPBuilder http, def temporaryFileOnServer, def bonitaToken) {
    http.request(POST) { multipartRequest ->
        // The REST resource URL to register a new REST API extension (REST API extension is handle as a page)
        uri.path = "/bonita/API/portal/page"

        // We send some JSON data
        requestContentType = JSON

        // Include API token in the header for authentication
        headers['X-Bonita-API-Token'] = bonitaToken

        // Request body includes information about the file previously uploaded (name of the temporary file on server side)
        body =  [pageZip: temporaryFileOnServer]

        // Handle the successful response
        response.success = { resp, reader ->
            if (resp.statusLine.statusCode == 200) {
                println "REST API successfully registered"
            }

            System.out << reader
        }
    }
}
