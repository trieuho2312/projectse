package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should create a user successfully"
    request {
        method POST()
        url "/users"
        headers {
            contentType(applicationJson())
        }
        body([
            username: "contractuser",
            password: "password123",
            fullname: "Contract Test User",
            email: "contract@sis.hust.edu.vn"
        ])
    }
    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
            userId: regex('[a-f0-9-]{36}'),
            username: "contractuser",
            fullname: "Contract Test User",
            email: "contract@sis.hust.edu.vn"
        ])
    }
}
