package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should get product by ID"
    request {
        method GET()
        urlPath("/products") {
            queryParameters {
                parameter("id", regex('[a-f0-9-]{36}'))
            }
        }
    }
    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
            productId: regex('[a-f0-9-]{36}'),
            name: anyNonBlankString(),
            price: $(consumer(regex('[0-9]+(\\.?[0-9]+)?')), producer(100000.0)),
            brand: anyNonBlankString()
        ])
    }
}
