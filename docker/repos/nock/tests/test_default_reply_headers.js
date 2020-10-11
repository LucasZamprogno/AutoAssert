'use strict'

const { expect } = require('chai')
const nock = require('..')
const got = require('./got_client')

require('./setup')

describe('defaultReplyHeaders()', () => {

  // Raw headers are an array of strings in [header, value, header, value, ...] order.
  it('when no headers are specified on the request, default reply headers work', async () => {
    nock('http://example.test')
      .defaultReplyHeaders({
        'X-Powered-By': 'Meeee',
        'X-Another-Header': ['foo', 'bar'],
      })
      .get('/')
      .reply(200, '')

    // Target variable is rawHeaders
    const { rawHeaders } = await got('http://example.test/')

  })
})
