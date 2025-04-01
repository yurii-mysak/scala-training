package persistence.model

case class State[+StateType](state: Option[StateType])
