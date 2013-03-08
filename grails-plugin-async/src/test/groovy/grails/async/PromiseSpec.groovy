package grails.async

import spock.lang.Specification

/**
 * @author Graeme Rocher
 */
class PromiseSpec extends Specification {

    void "Test promise map handling"() {
        when:"A promise map is created"
            def map = Promises.create(one: { 1 }, two: { 1 + 1 }, four:{2 * 2})
            def result = map.get()

        then:"The map is valid"
            result == [one: 1, two: 2, four: 4]
    }

    void "Test promise list handling"() {
        when:"A promise list is created from two promises"
            def p1 = Promises.create { 1 + 1 }
            def p2 = Promises.create { 2 + 2 }
            def list = Promises.create(p1, p2)

            def result
            list.onComplete { List v ->
                result = v
            }

            sleep 200
        then:"The result is correct"
            result == [2,4]

        when:"A promise list is created from two closures"
            list = Promises.create({ 1 + 1 }, { 2 + 2 })

            list.onComplete { List v ->
                result = v
            }

            sleep 200
        then:"The result is correct"
            result == [2,4]


    }

    void "Test promise onComplete handling"() {

        when:"A promise is executed with an onComplete handler"
            def promise = Promises.create { 1 + 1 }
            def result
            def hasError = false
            promise.onComplete { val ->
                result = val
            }
            promise.onError {
                hasError = true
            }
            sleep 1000

        then:"The onComplete handler is invoked and the onError handler is ignored"
            result == 2
            hasError == false


    }

    void "Test promise onError handling"() {

        when:"A promise is executed with an onComplete handler"
            def promise = Promises.create {
                throw new RuntimeException("bad")
            }
            def result
            Throwable error
            promise.onComplete { val ->
                result = val
            }
            promise.onError { err ->
                error = err
            }
            sleep 1000

        then:"The onComplete handler is invoked and the onError handler is ignored"
            result == null
            error != null
            error.message == "bad"
    }

    void "Test promise chaining"() {
        when:"A promise is chained"
            def promise = Promises.create { 1 + 1 }
            promise = promise.then { it * 2 } then { it + 6 }
            def val = promise.get()

        then:'the chain is executed'
            val == 10
    }
}

