/*
* Copyright 2021 Kári Magnússon
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package kuzminki.pekko

import scala.concurrent.ExecutionContext
import org.apache.pekko.actor.ActorSystem
import kuzminki.api.{Kuzminki, DbConfig}


object KuzminkiPekko {

  def create(conf: DbConfig)(implicit system: ActorSystem): Kuzminki = {
    val dbContext: ExecutionContext = system.dispatchers.lookup(
      "pekko.actor.default-blocking-io-dispatcher"
    )
    Kuzminki.create(conf, dbContext)
  }

  def createSplit(masterConf: DbConfig, slaveConf: DbConfig)(implicit system: ActorSystem): Kuzminki = {
    val dbContext: ExecutionContext = system.dispatchers.lookup(
      "pekko.actor.default-blocking-io-dispatcher"
    )
    Kuzminki.createSplit(masterConf, slaveConf, dbContext)
  }
}