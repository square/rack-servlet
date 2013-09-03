require 'sinatra/base'

class Application < Sinatra::Base
  get '/set-multiple-cookies' do
    response.set_cookie :foo, 'bar'
    response.set_cookie :bar, 'foo'
  end
end

Application.new