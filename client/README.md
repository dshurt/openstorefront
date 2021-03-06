# Client Developers Guide

## Login

The front facing login pages are being rewritten in Vue.js to be mobile friendly. These include a form for registration, forgotten password or username, and f.a.q. pages.

## Mobile

A small feature set of the desktop web application is being implemented in Vue.js to specifically target mobile users.

## Setup

The initial scaffolding was created using `vue init webpack <project name>` using the Standard ESLint configuration, NightWatch for E2E testing, and Jest for unit tests. We are also using NPM for package management.

In VSCode be sure to install the following plugins to ease development and ensure consistant coding styles:

- ESLint
- Vetur
- EditorConfig

### API Proxy

The proxy settings can be found in `config/index.js`. The current configuration is expecting a local instance of Storefront running on `localhost:8080`. You can however configure the proxy server to make requests to staging or any other running instance of Storefront. Just change the proxy table endpoint for storefront. When you run `npm run dev` you must first go to `localhost:3000/openstorefront/Login.Action` and login to establish a session with the server. You can then make proxied authenticated API calls to the server.