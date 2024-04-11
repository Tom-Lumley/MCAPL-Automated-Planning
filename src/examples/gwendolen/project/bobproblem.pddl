(define (problem textingproblem)
  (:domain shoppingdomain)
  (:init
    (belief1)
    (belief2)
    (belief3)
    (dummyPredicate)
    (hasPhone)
    (motivated)
    (onPhone)
    (parentsHappy)
    (dummyPredicate)
  )
  (:goal
    (and
      (messageSent)
    )
  )
)
