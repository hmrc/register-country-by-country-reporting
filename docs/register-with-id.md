Register-with-id
-----------------------
Register with id for UK Individual. This is for business matching.

* **URL**

  `/registration/utr`

* **Method**

  `POST`

* **Example Payload**

```json
{
  "registerWithIDRequest": {
    "requestCommon": {
      "receiptDate": "2023-01-23T08:32:48Z",
      "regime": "CBC",
      "acknowledgementReference": "test-acknowledgement"
    },
    "requestDetail": {
      "IDType": "UTR",
      "IDNumber": "test-IDNumber",
      "requiresNameMatch": true,
      "isAnAgent": false,
      "organisation": {
        "organisationName": "test-OrgName",
        "organisationType": "0001"
      }
    }
  }
}

```

* **Success Response:**

    * **Code:** 200 <br />

* **Example Success Response**

```json
{
  "registerWithIDResponse": {
    "responseCommon": {
      "status": "OK",
      "statusText": "Sample status text",
      "processingDate": "2023-01-23T08:32:48Z",
      "returnParameters": [
        {
          "paramName": "SAP_NUMBER",
          "paramValue": "0123456789"
        }
      ]
    },
    "responseDetail": {
      "SAFEID": "XE0000123456789",
      "ARN": "WARN8976221",
      "isEditable": true,
      "isAnAgent": false,
      "isAnIndividual": true,
      "individual": {
        "firstName": "Fred",
        "middleName": "Flintstone",
        "lastName": "Flint",
        "dateOfBirth": "1999-12-20"
      },
      "address": {
        "addressLine1": "22 livingston road",
        "addressLine2": "43",
        "addressLine3": "near A&E",
        "addressLine4": "Oxford",
        "postalCode": "OX2 3HD",
        "countryCode": "GB"
      },
      "contactDetails": {
        "phoneNumber": "020947376",
        "mobileNumber": "07634527721",
        "faxNumber": "02073648933",
        "emailAddress": "anr@wipro.com"
      }
    }
  }
}

```

* **Error Response:**

    * **Code:** 400 BAD_REQUEST <br />
      **Content:** `{"errorDetail": {
      "timestamp" : "2017-02-14T12:58:44Z",
      "correlationId": "c181e730-2386-4359-8ee0-
      f911d6e5f3bc",
      "errorCode": "400",
      "errorMessage": "Invalid ID",
      "source": "Back End",
      "sourceFaultDetail":{
      "detail":[
      "001 - Regime missing or invalid"
      ]}
      }}`

    * **Code:** 404 NOT_FOUND <br />
      **Content:** `{"errorDetail": {
      "timestamp" : "2017-02-14T12:58:44Z",
      "correlationId": "c181e730-2386-4359-8ee0-
      f911d6e5f3bc",
      "errorCode": "404",
      "errorMessage": "Record not found",
      "source": "journey-dct50b-service-camel",
      "sourceFaultDetail":{
      "detail":[
      "Record not found"
      ]}
      }}`

    * **Code:** 4XX Upstream4xxResponse <br />

  OR anything else

    * **Code:** 5XX Upstream5xxResponse <br />