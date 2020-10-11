'use strict'

const { expect } = require('chai')
const http = require('http')
const sinon = require('sinon')
const nock = require('..')

const got = require('./got_client')
const { startHttpServer } = require('./servers')

require('./setup')

describe('allowUnmocked option', () => {

  // When an endpoint mock is set up, requests to other endpoints should still go throgh and get proper responses
  it('allow unmocked post with json data', async () => {
    const { origin } = await startHttpServer((request, response) => {
      response.writeHead(200)
      response.write('{"message":"server response"}')
      response.end()
    })

    nock(origin, { allowUnmocked: true })
      .get('/not/accessed')
      .reply(200, '{"message":"mocked response"}')

    // Target variables is body
    const { body } = await got.post(origin, {json: { some: 'data' }, responseType: 'json',})
  })

})
