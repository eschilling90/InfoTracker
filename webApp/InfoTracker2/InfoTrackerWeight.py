from google.appengine.ext import ndb

import logging

class Weight(ndb.Model):

	username = ndb.StringProperty()
	weight = ndb.FloatProperty()
	date = ndb.DateTimeProperty(auto_now_add=True)
