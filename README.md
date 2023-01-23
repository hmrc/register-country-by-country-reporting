
Register Country By Country Reporting
==================================

Back-end microservice to support the registration of a multinational enterprise (MNE) group to send a Country By Country report, which allows HMRC to exchange data with international treaty partners including the EU, the OECD, and the USA.

API
---

| *Task*                                             | *Supported Methods* | *Description*                                                                                                                                                                                                                             |
|----------------------------------------------------|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ```/registration/noId                          ``` | POST                | Registers an organisation on ETMP who does not have a UTR. This will be a non-UK organisation [More...](docs/register-without-id.md)                                                                                                      |
| ```/registration/utr                           ``` | POST                | Registers an organisation on ETMP who has a UTR. This will be a UK organisation [More...](docs/register-with-id.md)                                                                                                                       |
| ```/subscription/create-subscription           ``` | POST                | Allows a registered customer to create a CbC Subscription channel. If a user is already subscribed, an error message is presented, if not, user is enrolled and their details also stored in EACD. [More...](docs/create-subscription.md) |
| ```/subscription/read-subscription/:safeId     ``` | POST                | Allows a registered customer to read and view their CbC Subscription information retained in ETMP. [More...](docs/create-subscription.md)                                                                                                 |

## Running the service

Service Manager full profile: CBCR_NEW_ALL

Service Manager service profile: REGISTER_COUNTRY_BY_COUNTRY_REPORTING

Port: 10027

Base URL: http://localhost:10027/register-country-by-country-reporting

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").