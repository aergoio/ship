--------------------------------------------------------------
--
-- Utility for assertion
--
function assertTrue(exp, message)
  if exp then
    return
  end

  if message then
    error(message, 0)
  else
    error("expression must be ture", 0)
  end
end

function assertFalse(exp, message)
  if not exp then
    return
  end

  if message then
    error(message, 0)
  else
    error("expression must be ture", 0)
  end
end

function assertNotEquals(a, b, message)
  if actual ~= expected then
    return
  end

  if message then
    error(message, 0)
  else
    error(tostring(actual) .. " is equal to " .. tostring(expected) .. ". Two value should not be equal.", 0)
  end

end

function assertEquals(expected, actual, message)
  if actual == expected then
    return
  end

  if message then
    error(message, 0)
  else
    error(tostring(actual) .. " is not equal to " .. tostring(expected) .. ". Expected: " .. tostring(expected) .. ", Actual: " .. tostring(actual), 0)
  end
end
--------------------------------------------------------------
--
-- Mock-up for aergo server environment
--
system = {}
function system.getItem(key)
  return value
end

function system.setItem(key, value)
end

function system.getSender()
  return address
end

function system.getCreator()
  return address
end
function system.getBlockhash()
  return hash
end
function system.getBlockheight()
  return height
end
function system.Timestamp()
end
function system.getContractID()
  return id
end
function system.getTxhash()
  return hash
end
function system.getNode()
  return id
end

abi = {}
function abi.register(funcname, ...)
end
--------------------------------------------------------------
--
-- Test framework for lua contract
--
require "ship.test.Athena"

TestCase = { }
TestCaseMetatable = { __index = TestCase }
setmetatable(TestCase, {
  __call = function(cls, ...)
    return cls.new(...)
  end
})

function TestCase.new(name, runnable)
  return setmetatable({
    name = name,
    runnable = runnable,
    error = nil
  }, TestCaseMetatable)
end

function TestCase:expected(error)
  self.error = error
  return self
end

function TestCase:run()
  TestReporter.startTest(self.name)
  local result, err = pcall(self.runnable)
  if self.error then
    if err then
      local handledResult = self.error(err)
      if not handledResult then
        TestReporter.recordError(self.name, 'User unexpected error')
      end
    else
      TestReporter.recordError(self.name, 'No error')
    end
  elseif err then
    TestReporter.recordError(self.name, 'Unexpected error')
  end
  TestReporter.endTest(self.name)
end

local TestSuite = { }
TestSuite.__index = TestSuite
setmetatable(TestSuite, {
  __call = function(cls, ...)
    return cls.new(...)
  end,
})
function TestSuite.new(name)
  local self = setmetatable({}, TestSuite)
  self.name = name
  self.testCases = {}
  return self
end

function TestSuite:run()
  local n = self.name
  TestReporter.startSuite(n)
  for name, testCase in pairs(self.testCases) do
    testCase:run()
  end
  TestReporter.endSuite(self.name)
end
function TestSuite:add(testCase)
  local name = testCase.name
  self.testCases[name] = testCase
end

function min(a, b)
  if (a<b) then
    return a
  else
    return b
  end
end

require "ship.test.Athena"

local suite = TestSuite('my-suite')
suite:add(TestCase('testcase', function()
  assertEquals(3, min(3, 4))
end))
suite:run()
