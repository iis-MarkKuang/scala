package com.shrfid.api.persistence.elastic4s

import javax.inject.Inject

import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api._
import com.shrfid.api.http.Elastic.BaseGetListRequest
import com.shrfid.api.modules.Elastic4SModule.Elastic4SDatabaseSource
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.get.RichGetResponse
import com.sksamuel.elastic4s.indexes.{CreateIndexDefinition, RichIndexResponse}
import com.sksamuel.elastic4s.jackson.ElasticJackson
import com.sksamuel.elastic4s.searches.{QueryDefinition, RichSearchResponse}
import com.sksamuel.elastic4s.update.RichUpdateResponse
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram

import scala.collection.JavaConverters._

/**
  * Created by jiejin on 24/11/16.
  */


trait BaseDoc {
  val datetime: String
}

trait BaseDal[T] {

  def id(doc: T): Future[String]

  def insertDoc(_doc: String): Future[IndexResponse]

  def insertDoc(_id: String, _doc: String): Future[IndexResponse]

  def insertDocReturnId(_doc: String): Future[(Int, String, String)]

  def upsertDoc(_id: String, _doc: T): Future[UpsertResponse]

  def upsertDoc(_id: String, _doc: String): Future[UpsertResponse]

  def upsertDoc(_id: String, _doc: T, _parent: String): Future[UpsertResponse]

  def updateDoc(_id: String, _doc: String): Future[UpsertResponse]

  def updateDoc(_id: String, _doc: Map[String, Any]): Future[UpsertResponse]

  def findById(_id: String, sourceIn: Seq[String], sourceEx: Seq[String]): Future[GetResponse]

  def findByIds(ids: Seq[String]): Future[RichSearchResponse]

  def findByIdRich(_id: String): Future[RichGetResponse]

  def findByIdAsMap(_id: String): Future[Option[Map[String, AnyRef]]]

  def findByIdAsOptString(_id: String): Future[Option[(Long, String)]]

  def findAllDocs(request: BaseGetListRequest): Future[SearchResponse]

  def findAll(_m: Seq[QueryDefinition], _n: Seq[QueryDefinition], _ex: Seq[String]): Future[RichSearchResponse]

  def findAll(request: BaseGetListRequest): Future[RichSearchResponse]

  def count(_m: Seq[QueryDefinition] = Seq(), _n: Seq[QueryDefinition] = Seq(), _ex: Seq[String] = Seq(),
            _offset: Int= 0, _limit: Int =10000): Future[Long]

  def deleteById(_id: String): Future[DeleteResponse]

  def deleteBulk(_ids: Seq[String]): Future[DeleteResponse]

  def isUnique(field: String, value: String): Future[Boolean]

  def isUnique(field: String, value: String, _id: String): Future[Boolean]

  def isExists(_id: String): Future[Boolean]

}


abstract class BaseDalImpl[T <: BaseDoc] @Inject()(db: Elastic4SDatabaseSource)(_index: String, _type: String) extends BaseDal[T] {


  import ElasticJackson.Implicits._

  val delimiter = ","


  override def insertDoc(_id: String, _doc: String): Future[IndexResponse] = {
    db.execute(
      indexInto(_index / _type).doc(_doc).id(_id)
    ).toTwitterFuture.map(a => (a.original.status().getStatus, indexJsonResponse(a)))
  }

  override def insertDoc(_doc: String): Future[IndexResponse] = {
    db.execute(
      indexInto(_index / _type).doc(_doc)
    ).toTwitterFuture.map(a => (a.original.status().getStatus, indexJsonResponse(a)))
  }
  override def insertDocReturnId(_doc: String): Future[(Int, String, String)] = {
    db.execute(
      indexInto(_index / _type).doc(_doc)
    ).toTwitterFuture.map(a => (a.original.status().getStatus, indexJsonResponse(a), a.id))
  }
  override def upsertDoc(_id: String, _doc: T): Future[UpsertResponse] = {
    db.execute(
      update(_id).in(_index / _type).docAsUpsert(_doc)
    ).toTwitterFuture.map(a => (a.status.getStatus, upsertJsonRepsonse(a)))
  }

  override def upsertDoc(_id: String, _doc: String): Future[UpsertResponse] = {
    //println("upserting")
    db.execute(
      update(_id).in(_index / _type).docAsUpsert(_doc)
    ).toTwitterFuture.map(a => (a.status.getStatus, upsertJsonRepsonse(a)))
  }

  override def upsertDoc(_id: String, _doc: T, _parent: String): Future[UpsertResponse] = {
    db.execute(
      update(_id).in(_index / _type).docAsUpsert(_doc).parent(_parent)
    ).toTwitterFuture.map(a => (a.status.getStatus, upsertJsonRepsonse(a)))
  }

  override def updateDoc(_id: String, _doc: String): Future[UpsertResponse] = {
    db.execute(
      update(_id).in(_index / _type).doc(_doc)
    ).toTwitterFuture.map(a => (a.status.getStatus, updateJsonRepsonse(a)))
  }

  override def updateDoc(_id: String, _doc: Map[String, Any]): Future[UpsertResponse] = {
    db.execute(
      update(_id).in(_index / _type).doc(_doc)
    ).toTwitterFuture.map(a => (a.status.getStatus, updateJsonRepsonse(a)))
  }


  def indexJsonResponse(response: RichIndexResponse): String = {
    s"""{"id": "${response.id}", "result": "${response.result.getLowercase}", "created": ${response.created} }"""
  }

  def upsertJsonRepsonse(response: RichUpdateResponse): String = {
    s"""{"id": "${response.id}", "result": "${response.result.getLowercase}", "created": ${response.created}}"""
  }

  def updateJsonRepsonse(response: RichUpdateResponse): String = {
    s"""{"id": "${response.id}", "result": "${response.result.getLowercase}", "updated": ${
      response.result.getLowercase match {
        case "updated" => true
        case _ => false
      }
    } }"""
  }


  override def findAllDocs(request: BaseGetListRequest): Future[SearchResponse] = {
    findAll(request).map(a => (a.original.status().getStatus, searchJsonResponse(a))).map(result =>
      (result._1, request.response(result._2._1, result._2._2)))
  }

  override def findAll(_m: Seq[QueryDefinition] = Seq(), _n: Seq[QueryDefinition] = Seq(), _ex: Seq[String] = Seq()): Future[RichSearchResponse] = {
    val result = db.execute(
      search(_index / _type) query {
        boolQuery().must(_m).not(_n)
      } start 0 limit 10000
    )
    result.toTwitterFuture
  }

  override def findAll(request: BaseGetListRequest): Future[RichSearchResponse] = {
    val _m = request.filter.must.filterNot(_ == None).asInstanceOf[Seq[QueryDefinition]]
    val _n = request.filter.not.filterNot(_ == None).asInstanceOf[Seq[QueryDefinition]]
    val _ex = request.filter.sourceExclude

    val query = search(_index / _type) query {
      boolQuery().must(_m).not(_n)
    } sourceExclude _ex
    val query1 = request.sortBy match {
      case None => query
      case Some(_orderBy) => query sortBy _orderBy
    }
    val query2 = request.getLimit match {
      case -1 => query1 limit 10000 // ... arbitrary Num.
      case other => query1 start request.getOffset limit request.getLimit
    }
    //println(query2 .show)
    db.execute(query2).toTwitterFuture

  }

  override def findByIds(ids: Seq[String]): Future[RichSearchResponse] = {
    val query = search(_index / _type) query boolQuery().filter(termsQuery("_id", ids))
    //println(query.show)
    val result = db.execute(query).toTwitterFuture
    result
  }

  def searchJsonResponse(a: RichSearchResponse): (Int, String) = {
    (a.totalHits.toInt,
      s"""[${
        a.hits.map(
          a => a.sourceAsString.replaceFirst("\\{", s"""{ "id":"${a.id}", """)).mkString(",")
      }]""")
  }

  def searchJsonResponseWithout(a: RichSearchResponse): (Int, String) = {
    (a.totalHits.toInt,
      s"""[${
        a.hits.map(
          a => a.sourceAsString).mkString(",")
      }]""")
  }

  override def findById(_id: String, sourceIn: Seq[String] = Seq("*"), sourceEx: Seq[String]= Seq()): Future[GetResponse] = {
    val query = get(_id) from (_index / _type) fetchSourceContext(sourceIn, sourceEx)
    val result = db.execute(query )
    result.toTwitterFuture.map(a => a.exists match {
      case true => (Status.Ok.code, a.sourceAsString.replaceFirst("\\{", s"""{ "id":"${a.id}", """))
      case false => (Status.NotFound.code, NotFound)
    })
  }

  override def findByIdRich(_id: String): Future[RichGetResponse] = {
    val query = get(_id) from (_index / _type)
    val result = db.execute(query )
    result.toTwitterFuture
  }

  def findByIdAsMap(_id: String): Future[Option[Map[String, AnyRef]]] = {
    db.execute(get(_id) from (_index / _type)).toTwitterFuture.map(a => a.exists match {
      case false => None
      case true => Some(a.sourceAsMap)
    })
  }

  override def findByIdAsOptString(_id: String): Future[Option[(Long, String)]] = {
    db.execute(get(_id) from (_index / _type)).toTwitterFuture.map(a => a.exists match {
      case false => None
      case true => Some((a.version, a.sourceAsString))
    })
  }

  override def count(_m: Seq[QueryDefinition] = Seq(), _n: Seq[QueryDefinition] = Seq(), _ex: Seq[String] = Seq()
                     , _offset: Int= 0, _limit: Int =10000): Future[Long] = {
    val result = db.execute(
      search(_index / _type) query {
        boolQuery().must(_m).not(_n)
      } start _offset limit _limit fetchSource false
    )
    result.toTwitterFuture.map(a => a.totalHits)

  }


  def deleteJsonRepsonse(response: org.elasticsearch.action.delete.DeleteResponse): DeleteResponse = {
    (response.getResult.getLowercase match {
      case "not_found" => Status.NotAcceptable.code
      case "deleted" => Status.Accepted.code
      case _ => Status.Ok.code
    },
      s"""{"id": "${response.getId}", "result": "${response.getResult.getLowercase}" , "deleted": ${
        response.getResult.getLowercase match {
          case "deleted" => true
          case _ => false
        }
      }}""")
  }

  override def deleteById(_id: String): Future[DeleteResponse] = {
    db.execute(
      delete(_id) from (_index / _type)
    ).toTwitterFuture.map(a => deleteJsonRepsonse(a))
  }

  override def deleteBulk(_ids: Seq[String]): Future[DeleteResponse] = {
    Future.collect(_ids.map(id => deleteById(id))).map(response =>
      (Status.Ok.code, s"""[${response.map(e => e._2).mkString(",")}]"""))
  }

  override def isUnique(field: String, value: String): Future[Boolean] = {
    db.execute(
      search(_index / _type) query {
        boolQuery().must(matchPhraseQuery(field + ".keyword", value))
      } fetchSource false
    ).toTwitterFuture.map(a => if (a.totalHits == 0) true else false)
  }

  override def isUnique(field: String, value: String, _id: String): Future[Boolean] = {
    db.execute(
      search(_index / _type) query {
        boolQuery().must(matchPhraseQuery(field+ ".keyword", value)).not(matchPhraseQuery("_id", _id))
      } fetchSource false
    ).toTwitterFuture.map( a=> if (a.totalHits == 0) true else false)
  }

  // added by kuang yuan 5/5/2017
  def isUniqueWithConditions(field: String, value: String, _id: String, conditions: Seq[QueryDefinition]): Future[Boolean] = {
    db.execute(
      search(_index / _type) query {
        boolQuery().must(matchPhraseQuery(field + ".keyword", value) +: conditions).not(matchPhraseQuery("_id", _id))
      }
    ).toTwitterFuture.map( a => if (a.totalHits == 0) true else false)
  }

  override def isExists(_id: String): Future[Boolean] = {
    db.execute(
      get(_id) from (_index / _type)
    ).toTwitterFuture.map( a=> a.exists)
  }

  // added by yuan kuang 4/12/2017

  def updateDocWithResult(_id: String, _doc: Map[String, Any], key: String, value: String): Future[UpsertResponse] = {
    db.execute(
      update(_id).in(_index / _type).doc(_doc)
    ).toTwitterFuture.map(a => (a.status.getStatus, updateJsonRepsonseWithAdditionalField(a, key, value)))
  }


  // now this is restricted to bucket aggregations, pls don't use it for metrics ones.
  def searchNestedAggResponse(a: RichSearchResponse,
                              agg_names: Seq[String],
                              result_name: String,
                              bucket_key: String,
                              bucket_values: Seq[String],
                              sub_agg_bucket_key: String,
                              sub_agg_bucket_value: String): (Int, String) = {
    //TODO add params for agg types
    (200,
      s"""{"$result_name": [${
        (agg_names, bucket_values).zipped.map((agg_name, bucket_value) =>
          s"""{"$agg_name": [${
        a.aggregations.histogramResult(agg_name).getBuckets.asScala.map(
          bucket =>
            if (bucket.getAggregations.asScala.head.asInstanceOf[Terms].getBuckets.size > 0)
            s"""{"$bucket_key": "${bucket.getKeyAsString}", \r\n
            "$bucket_value": [${bucket.getAggregations.asScala.map(
            ag => ag.asInstanceOf[Terms].getBuckets.asScala.map(
              b => s"""{"$sub_agg_bucket_key": "${b.getKeyAsString}", \r\n
                     "$sub_agg_bucket_value": "${b.getDocCount.toString}"}"""
            ).mkString(delimiter)
            //            ag => ag.asInstanceOf[Terms].getBuckets.size match {
            //                  case 0 => ""
            //                  case a: Int if a > 0 => ag.asInstanceOf[Terms].getBuckets.asScala.map(
            //                    b => s"""{"category": "${b.getKeyAsString}", \r\n
            //                         "doc_count": "${b.getDocCount.toString}"}"""
            //                  ).mkString(delimiter)
            //                }
          ).mkString(delimiter)}
            \r\n]}"""
          else ""
        ).filter(a => !a.isEmpty).mkString(delimiter)
          }]}"""
        ).mkString(delimiter)
      }]}""")

  }

  def searchHistogramAggResponse(a: RichSearchResponse, agg_name: String, result_name: String, bucket_key: String, bucket_value: String): (Int, String) = {
    (200,
      s"""{"$result_name": [${
        a.aggregations.histogramResult(agg_name).getBuckets.asScala.map(
          bucket => s"""{"$bucket_key": "${bucket.getKeyAsString}", \r\n
          "$bucket_value": "${bucket.getDocCount.toString}"}\r\n"""
        ).mkString(delimiter)
      }]}""")
  }

  def searchTermsAggResponse(a: RichSearchResponse, agg_name: String, result_name: String, bucket_key: String, bucket_value: String): (Int, String) = {
    (200,
      s"""{"$result_name": [${
        a.aggregations.termsResult(agg_name).getBuckets.asScala.map(
          //        bucket => bucket.getKeyAsString + " " + bucket.getAggregations.get("borrow_times").getProperty("value")
          bucket => s"""{"$bucket_key": "${bucket.getKeyAsString}", \r\n
          "$bucket_value": "${bucket.getDocCount.toString}"}\r\n"""
        ).mkString(delimiter)
      }]}""")
    // added
    //      for {
    //        result <- a.aggregations.aggregations.asList().toArray.head
    ////        result <- a.aggregations.aggregations.getProperty("buckets")
    //      } yield result

    //      JavaConverters.iterableAsScalaIterableConverter(a.aggregations.aggregations).asScala.map(a =>
    //        a.getProperty("bookRanking").toString
    //      ).mkString(",")
  }


  def updateJsonRepsonseWithAdditionalField(response: RichUpdateResponse, key: String, value: String): String = {
    s"""{"id": "${response.id}", "result": "${response.result.getLowercase}", "updated": ${
      response.result.getLowercase match {
        case "updated" => true
        case _ => false
      }

    }, "${key}": "${value}" }"""
  }
}
