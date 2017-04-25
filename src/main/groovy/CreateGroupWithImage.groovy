#!/usr/bin/env groovy

import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.*

import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.ContentType


HTTPBuilder http = new HTTPBuilder('http://localhost:8080')

// Login
def bonitaToken = login(http)

// Upload the group icon image file to a temporary folder on server side
def temporaryFileOnServer = uploadImage(http)

// Creation of REST API extension using the file previously upload in temporary folder
createNewGroup(http, temporaryFileOnServer, bonitaToken)

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

// Image upload using imageUpload servlet to a temporary folder
def uploadImage(HTTPBuilder http) {

  def uploadResponse

  http.request(POST) { multipartRequest ->
    // URL of image upload servlet
    uri.path = "/bonita/portal/imageUpload"

    // We use multipart content type to upload the image
    requestContentType = "multipart/form-data"

    // Create a multipart entity builder for our local test file
    MultipartEntityBuilder multiPartContent = MultipartEntityBuilder.create().addBinaryBody("icon", new FileInputStream("src/main/groovy/icon.jpg"), ContentType.create("image/jpeg"), "icon.jpg")

    multipartRequest.entity = multiPartContent.build()

    // Handle successful response
    response.success = { resp, reader ->
      if (resp.statusLine.statusCode == 200) {
        // Store response information that include information about temporary storage of the file on server side
        uploadResponse = reader.text
      } else {
        println resp.statusLine.statusCode
      }
    }
  }

  if(uploadResponse == null) {
    throw new Exception("Failed to upload the image")
  } else {
    return uploadResponse
  }
}

void createNewGroup(HTTPBuilder http, def temporaryFileOnServer, def bonitaToken) {
  http.request(POST) { multipartRequest ->
    // The REST resource URL to create a new Group
    uri.path = "/bonita/API/identity/group"

    // We send some JSON data
    requestContentType = JSON

    // Include API token in the header for authentication
    headers['X-Bonita-API-Token'] = bonitaToken

    // Request body includes information about the file previously uploaded (name of the temporary file on server side)
    body =  [icon: temporaryFileOnServer, name: 'test', displayName: 'Test group', description: 'Group created using REST API']

    // Handle the successful response
    response.success = { resp, reader ->
      if (resp.statusLine.statusCode == 200) {
        println "Group successfully created"
      }

      System.out << reader
    }
  }
}
