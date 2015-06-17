from google.appengine.ext import ndb

import logging

class User(ndb.Model):

	name = ndb.StringProperty()
	username = ndb.StringProperty()
	password = ndb.StringProperty()
