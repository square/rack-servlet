class EnumerableWithClose
  attr_reader :open
  def initialize(list)
    @list = list
    @open = true
  end

  include Enumerable

  def each(&block)
    @list.each(&block)
  end

  def close
    @open = false
  end
end