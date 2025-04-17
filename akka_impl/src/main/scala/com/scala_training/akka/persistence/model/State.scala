package com.scala_training.akka.persistence.model

case class State[+StateType](state: Option[StateType])
