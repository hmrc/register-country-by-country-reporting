Register-without-id
-----------------------
Register without Id for NON-UK Organisation. This is for business matching.

* **URL**

  `/registration/noId`

* **Method**

  `POST`

* **Example Payload**

```json
{
  "registerWithoutIDRequest": {
    "requestCommon": {
      "receiptDate": "2023-01-23T09:08:50Z",
      "regime": "CBC",
      "acknowledgementReference": "test-acknowledgement"
    },
    "requestDetail": {
      "organisation": {
        "organisationName": "test-OrgName"
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
      },
      "identification": {
        "idNumber": "test-ID",
        "issuingInstitution": "test-institution",
        "issuingCountryCode": "GB"
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
  "registerWithoutIDResponse": {
    "responseCommon": {
      "status": "OK",
      "processingDate": "2023-01-23T09:08:50Z",
      "returnParameters": [
        {
          "paramName": "SAP_NUMBER",
          "paramValue": "0123456789"

        }
      ]
    },
    "responseDetail": {
      "SAFEID": "XE0000123456789",
      "ARN": "ZARN1234567"
    }
  }
}
```

* **Error Response:**

    * **Code:** 400 BAD_REQUEST <br />
      **Content:** `{
      "errorDetail" : {
      "errorCode" : 400,
      "errorMessage" : "Invalid JSON document.",
      "source" : "journey-dct50a-service-camel",
      "sourceFaultDetail" : {
      "detail" : [ "instance value (\"cbc\") not found in enum (possible values: [\"CbC\"])" ]
      },
      "timestamp" : "2020-09-25T21:54:12.015Z",
      "correlationId" : "1ae81b45-41b4-4642-ae1c-db1126900001"
      }
      }`

    * **Code:** 4XX Upstream4xxResponse <br />

  OR anything else

    * **Code:** 5XX Upstream5xxResponse <br />