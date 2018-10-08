function min(a, b)
  if (a<b) then
    return a
  else
    return b
  end
end

import "ship.test.Athena"

local suite = TestSuite('my-suite')
suite:add(TestCase('testcase', function()
  assertEquals(3, min(3, 4))
end))
suite:run()