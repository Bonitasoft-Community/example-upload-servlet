#!/usr/bin/env groovy

import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.*

import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.ContentType


HTTPBuilder http = new HTTPBuilder('http://localhost:8080')

// Login
def bonitaToken = login(http)


// Instantiate a process
for(int i = 0; i < 1; i++) {
	instantiateProcess(http, bonitaToken)
}

def login(HTTPBuilder http) {
	def bonitaToken
	
	http.request(POST) { multipartRequest ->
		// Login service URL
		uri.path = "/bonita/loginservice"

		// Content need to be URL encoded
		requestContentType = URLENC

		// User credentials
		body =  [username: 'asanto', password: 'bpm']

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


void instantiateProcess(HTTPBuilder http, def bonitaToken) {
    http.request(POST) { multipartRequest ->
        // The REST resource URL to register a new REST API extension (REST API extension is handle as a page)
        uri.path = "/bonita/API/bpm/process/"+"5251062969626131644"+"/instantiation"
        
        println uri.path

        // We send some JSON data
        requestContentType = JSON

        // Include API token in the header for authentication
        headers['X-Bonita-API-Token'] = bonitaToken

        // Request body includes information about the file previously uploaded (name of the temporary file on server side)
        //body = [] 
       // ['description': 'test', 'supplierIds': 3, 'summary': 'test']
       
       body = [
"reverseFlowInput" : [
"orderDate" : null,
"processType" : "GMP",
"customerAccountId" : "64648",
"sapOrderNumber" : "JFEZU82821921",
"requestedTransporter" : "UPS",
"supportTicketNumber": "1234",
"caseOwnerEmail": "Imad@rs2i.fr",
"currency" : "EUR",
"originSystem" : "CEGID" ,
"originStoreCode" : "UK01",
"originType" : "ORDER",
"originId" : "FFO-UK01-2049",
"customerAddress" :
[ "addressLine1" : "5 blank street", "addressLine2" : "building 1", "postCode" : "84088", "city" : "Beaulieu1", "country" : "CA", "stateCountry" : "CA", "firstname" : "Alex", "lastname" : "Fake", "company" : "", "phoneNumber" : "46705101869", "email" : "alexis@rs2i.fr", "communicationLanguage" : "EN" ]

,
"addressPickUp" :
[ "addressLine1" : "6 blank street", "addressLine2" : "building 2", "postCode" : "84089", "city" : "Beaulieu2", "country" : "FR", "stateCountry" : "", "firstname" : "Alex", "lastname" : "Fake", "company" : "", "phoneNumber" : "46705101869", "email" : "alexis@rs2i.fr", "communicationLanguage" : "EN" ]

,
"addressReturnKit" :
[ "addressLine1" : "7 blank street", "addressLine2" : "building 3", "postCode" : "84090", "city" : "Beaulieu3", "country" : "SG", "stateCountry" : "SG", "firstname" : "Alex", "lastname" : "Fake", "company" : "", "phoneNumber" : "46705101869", "email" : "alexis@rs2i.fr", "communicationLanguage" : "EN" ]

,
"addressRepairedProduct" :
[ "addressLine1" : "8 blank street", "addressLine2" : "building41", "postCode" : "84091", "city" : "Beaulieu4", "country" : "SG", "stateCountry" : "SG", "firstname" : "Alex", "lastname" : "Fake", "company" : "", "phoneNumber" : "46705101869", "email" : "alexis@rs2i.fr", "communicationLanguage" : "EN" ]

,
"products" : [
[ "orderId" : 2058526573, "originalSerialNumber1" : "H41E00223EW02", "originalSerialNumber2" : "H41E00223EW02", "originalProductId" : "GOLDRW", "customerNeedsCaseSurpack" : "FAUX", "customerNeedsCoverCase" : "FAUX", "customerNote" : "customer note !", "communicationLanguage" : "EN" ]

,
[ "orderId" : "2058526573", "originalSerialNumber1" : "H50L00713EW02", "originalSerialNumber2" : "H50L00713EW02", "originalProductId" : "GOLDRS", "customerNeedsCaseSurpack" : "FAUX", "customerNeedsCoverCase" : "FAUX", "customerNote" : "", "communicationLanguage" : "EN" ]

]
]
]

        // Handle the successful response
        response.success = { resp, reader ->
            if (resp.statusLine.statusCode == 200) {
                println "New instance of process"
            }

            System.out << reader
        }
    }
}
