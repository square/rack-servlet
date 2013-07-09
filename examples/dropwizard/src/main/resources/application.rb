require 'sinatra/base'

class Application < Sinatra::Base
  get '/hello' do
    'Hello, World!'
  end
end