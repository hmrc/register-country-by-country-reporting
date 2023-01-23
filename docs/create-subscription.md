Create-subscription
-----------------------
Accepts an invitation

* **URL**

  `/subscription/create-subscription`

* **Method**

  `POST`

* **Example Payload**

```json
{
  "createSubscriptionForCBCRequest": {
    "requestCommon": {
      "regime": "CBC",
      "receiptDate": "2020-09-12T18:03:45Z",
      "acknowledgementReference": "abcdefghijklmnopqrstuvwxyz123456",
      "originatingSystem": "MDTP"
    },
    "requestDetail": {
      "IDType": "SAFE",
      "IDNumber": "AB123456Z",
      "tradingName": "Tools for Traders Limited",
      "isGBUser": true,
      "primaryContact": {
        "individual": {
          "firstName": "John",
          "lastName": "Smith"
        },
        "email": "john@toolsfortraders.com",
        "phone": "0188899999",
        "mobile": "07321012345"
      },
      "secondaryContact": {
        "organisation": {
          "organisationName": "Tools for Traders"
        },
        "email": "contact@toolsfortraders.com",
        "phone": "+44 020 39898980"
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
  "createSubscriptionForCBCResponse": {
    "responseCommon": {
      "status": "OK",
      "processingDate": "1000-01-01T00:00:00Z"
    },
    "responseDetail": {
      "subscriptionID": "XACBC0000123456"
    }
  }
}
```

* **Error Response:**

    * **Code:** 400 BAD_REQUEST <br />
      **Content:** `{
      "errorDetail": {
      "timestamp": "2016-10-10T13:52:16Z",
      "correlationId": "d60de98c-f499-47f5-b2d6-e80966e8d19e",
      "errorCode": "400",
      "errorMessage": "REGIME Missing or Invalid",
      "source": "Back End",
      "sourceFaultDetail": {
      "detail": [
      "001 - REGIME Missing or Invalid"
      ]
      }
      }
      }`

    * **Code:** 404 NOT_FOUND <br />
      **Content:** `{
      "errorDetail": {
      "timestamp": "2016-10-10T13:52:16Z",
      "correlationId": "d60de98c-f499-47f5-b2d6-e80966e8d19e",
      "errorCode": "404",
      "errorMessage": "Record not found",
      "source": "Back End",
      "sourceFaultDetail": {
      "detail": [
      "Record not found"
      ]
      }
      }
      }`

    * **Code:** 4XX Upstream4xxResponse <br />

  OR anything else

    * **Code:** 5XX Upstream5xxResponse <br />