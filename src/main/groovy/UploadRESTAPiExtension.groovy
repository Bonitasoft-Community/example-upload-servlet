#!/usr/bin/env groovy

import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.*

import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.LaxRedirectStrategy
import org.apache.http.entity.ContentType


def http = new HTTPBuilder("http://localhost:8080")
//http.client.setRedirectStrategy(new LaxRedirectStrategy())

// Login
cookies = []
def bonitaToken
http.request(POST) { multipartRequest ->
  uri.path = "/bonita/loginservice"
  requestContentType = URLENC

  body =  [username: 'walter.bates', password: 'bpm']

  response.success = { resp, reader ->
    println "Login successful"
    resp.getHeaders('Set-Cookie').each {
      def splittedValue = it.value.split(";")
      def cookie = splittedValue.toString()
      cookies.add(cookie)
      println splittedValue[0]
      if(splittedValue[0].startsWith('X-Bonita-API-Token')) {
        println "token " + splittedValue[0]
        def token = splittedValue[0].split('=')
        println token
        bonitaToken = token[1]
      }
    }
  }
}

// File upload using pageUpload servlet to a temporary folder
def uploadResponse

http.request(POST) { multipartRequest ->
  uri.path = "/bonita/portal/pageUpload"
  requestContentType = "multipart/form-data"

  MultipartEntityBuilder multiPartContent = MultipartEntityBuilder.create().addBinaryBody("file", new FileInputStream("test.zip"), ContentType.create("application/zip"), "test.zip")

  multipartRequest.entity = multiPartContent.build()

  response.success = { resp, reader ->
    if (resp.statusLine.statusCode == 200) {
        println "OK"
    } else {
      println resp.statusLine.statusCode
    }

    uploadResponse = reader.text
  }
}

// Creation of REST API extension using the file previously upload in temporary folder
http.request(POST) { multipartRequest ->
  uri.path = "/bonita/API/portal/page"
  requestContentType = JSON
  headers['Cookie'] = cookies.join(';')
  headers['X-Bonita-API-Token'] = bonitaToken

  //def (tempFileName, fileName) = uploadResponse.split('::')

  //body =  [pageZip: tempFileName+':'+fileName]
  body =  [pageZip: uploadResponse]

  response.success = { resp, reader ->
    if (resp.statusLine.statusCode == 200) {
        println "Login successful"
    }

    System.out << reader
  }
}
