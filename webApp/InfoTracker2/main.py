#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
import webapp2
import json
import urllib
import logging
import time
import datetime
import re
import random

from google.appengine.api import urlfetch
from google.appengine.ext import ndb

from InfoTrackerUsers import User as itUser
from InfoTrackerWeight import Weight as itWeight


class LoginHandler(webapp2.RequestHandler):
	def get(self):
		self.response.write('In LoginHandler')

	def post(self):
		statusCode = 202
		username = self.request.get('username')
		password = self.request.get('password')
		user = itUser.query(itUser.username == str(username)).get()
		if user and user.password == password:
			statusCode = 200
		elif user and user.password != password
			statusCode = 201
		self.response.write(json.dumps({'statusCode': statusCode}))

class RegisterHandler(webapp2.RequestHandler):
	def get(self):
		self.response.write('In RegisterHandler')

	def post(self):
		statusCode = 202
		name = self.request.get('name')
		username = self.request.get('username')
		password = self.request.get('password')
		newUser = itUser(name = name, username = username, password = password,reportRate = 0)
		if newUser:
			newUser.put()
			statusCode = 200

		self.response.write(json.dumps({'statusCode': statusCode}))

class WeightHandler(webapp2.RequestHandler):
	def get(self):
		username = str(self.request.get('username'))
		dateSent = True
		try:
			date = str(self.request.get('date'))
		except KeyError:
			dateSent = False
		all_weight = []
		if dateSent:
			for weight in itWeight.query(itWeight.username == username):
				all_weight.append(weight)
			all_weight = sorted(all_weight,key=lambda r: r.date,reverse=True)
		else:
			str s = ""
		return_list = []
		for weight in all_weight:
			return_list.append({'weight': weight.weight, 'date': weight.date})
		self.response.write(json.dumps(return_list))

	def post(self):
		statusCode = 202
		username = str(self.request.get('username'))
		weight = float(self.request.get('weight'))

		newWeight = itWeight(weight = weight, username = username)
		if newWeight:
			newWeight.put()
			statusCode = 200

		self.response.write(json.dumps({'statusCode': statusCode}))

app = webapp2.WSGIApplication([
    ('/login', LoginHandler),
    ('/register'), RegisterHandler),
	('/weight'), WeightHandler)
], debug=True)
