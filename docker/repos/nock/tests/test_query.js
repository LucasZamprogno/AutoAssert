'use strict'

const { expect } = require('chai')
const sinon = require('sinon')
const url = require('url')
const nock = require('..')
const got = require('./got_client')
const assertRejects = require('assert-rejects')

require('./setup')

describe('`query()`', () => {
  describe('when called with an object', () => {

    // A mocked endpoint should be able to respond to a simple request
    it('matches a query string of the same name=value', async () => {
      const scope = nock('http://example.test')
        .get('/')
        .query({ foo: 'bar' })
        .reply()
      
      // Target variable is statusCode
      const { statusCode } = await got('http://example.test/?foo=bar')

      scope.done()
    })
  })
})
