Change Log
==========

Version 1.4 *(2013-11-07)*
----------------------------

* Fix bug where Rack can provide a String status code.

Version 1.3 *(2013-10-04)*
----------------------------

* Flush the response early after writing response headers.

Version 1.2 *(2013-09-03)*
----------------------------

* Support headers with multiple values. ("Set-Cookie," for example.)

Version 1.1 *(2013-08-28)*
----------------------------

* When the servlet is mounted at `/foo/*`, requests to `/foo` now succeed, setting `PATH_INFO` to the empty string.

Version 1.0 *(2013-07-10)*
----------------------------

Initial release.
