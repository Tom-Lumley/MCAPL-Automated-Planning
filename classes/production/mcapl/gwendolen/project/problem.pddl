(define (problem textingproblem)
  (:domain shoppingdomain)
  (:init
    (belief1)
    (belief2)
    (belief3)
    (dummyPredicate)
    (motivated)
    (dummyPredicate)
  )
  (:goal
    (and
      (hasPhone)
      (messageSent)
    )
  )
)
