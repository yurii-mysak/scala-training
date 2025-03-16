package com.github.scala_training
package persistence.model

final case class State[StateType](state: Option[StateType])
