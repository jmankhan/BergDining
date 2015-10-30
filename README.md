# Berg Dining Server
This server will scrape and cache various Sodexo backed websites and cache the results on a roughly weekly basis. A mobile app will request the data through assigned endpoints based on location (e.g. /berg).

Features:
* Endpoints per location
* Cached scrapings for fast response
* Communicate through HTTP, possibly add HTTPS
* Support for any platform that can read HTTP (which is pretty much all of them)
