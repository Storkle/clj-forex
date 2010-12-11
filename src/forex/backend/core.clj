(ns forex.backend.core)
(defprotocol backend
  (get-stream [symbol timeframe]) ;;return a PriceStream wrapper and start updating the PriceStream in the service
  (start [params]) ;;start any required services, including price stream updating and in mql case, sockets to get all other data
  (stop [params]) ;;stop services
  ) ;;and later on, we will add other stuff required for a backend, like getting account balance and placing orders

 