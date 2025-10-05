import random
from locust import task, FastHttpUser

# 초당 100명씩 2번 쿠폰을 발급받는다
class CouponIssueV1(FastHttpUser):
    connection_timeout =10.0
    network_timeout =10.0

    @task
    def issue(self):
        payload = { "userId": random.randint(1, 1000000)
        ,"couponId": 1
        }
        with self.rest("POST", "/v1/issue-async", json=payload) :
            pass