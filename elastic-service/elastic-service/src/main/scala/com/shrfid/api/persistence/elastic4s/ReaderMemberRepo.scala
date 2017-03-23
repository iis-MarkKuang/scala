package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api.domains.reader.ReaderMember
import com.shrfid.api.modules.Elastic4SModule._
import com.sksamuel.elastic4s.ElasticDsl._
import com.twitter.util.Future

/**
  * Created by jiejin on 8/12/16.
  */
@Singleton
class ReaderMemberRepo @Inject()(db: Elastic4SDatabaseSource) {
  val _index = "reader"
  val _type = "info"

  val dal = new BaseDalImpl[ReaderMember](db)(_index, _type) {
    override def id(doc: ReaderMember): Future[String] = Future.value(doc.identity)

    def findIdsByGroupId(groupIds: Seq[String]): Future[Seq[String]] = {
      val query = search(_index / _type) query {
        boolQuery().should(groupIds.distinct.map(g=> matchPhraseQuery("groups", g))).not(matchPhraseQuery("is_active", false))
      } fetchSource false start 0 limit 10000
      //println(query.show)
     db.execute(query).toTwitterFuture.map(a => a.ids.distinct)
    }
  }
}
