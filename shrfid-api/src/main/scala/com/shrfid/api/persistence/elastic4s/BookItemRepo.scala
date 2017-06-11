package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api._
import com.shrfid.api.domains.book.Book
import com.shrfid.api.modules.Elastic4SModule._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType
import com.twitter.util.Future
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.search.sort.SortOrder
import play.api.libs.json.Json

import scala.collection.JavaConversions._


/**
  * Created by jiejin on 2/12/16.
  */
@Singleton
class BookItemRepo @Inject()(db: Elastic4SDatabaseSource) {
  val _index = "book"
  val _type = "info"

  val dal = new BaseDalImpl[Book](db)(_index, _type) {
    override def id(doc: Book): Future[String] = Future.value(doc.barcode.toString)

    override def upsertDoc(_id: String, _doc: Book): Future[UpsertResponse] = {
      db.execute(
        update(_doc.barcode.toString).in(_index / _type).docAsUpsert(_doc.jsonStringify)
      ).toTwitterFuture.map(a => (a.status.getStatus, upsertJsonRepsonse(a)))
    }

    def findNextBookIndex(clc: String) = {
      db.execute(
        search(_index / _type) termQuery("clc.keyword", clc) fetchSource (false) aggregations (
          maxAggregation("book_index").field("book_index")
        )
      ).toTwitterFuture.map(a => a.aggregations.maxResult("book_index").value() match {
        case a if a.isInfinity => 1
        case b => b.toInt
      })
    }

    def findMaxBarcode(category: String): Future[String] = {

      db.execute(indexExists("book")).toTwitterFuture.flatMap {
        case a => a.isExists match {
          case true =>
            db.execute(
              search(_index / _type) termQuery("category.keyword", category) sortBy (Iterable(fieldSort("barcode") order SortOrder.DESC)) limit 1
            ).toTwitterFuture.map(a =>
              a.hits.headOption match {
                case None => barcodeFmt(0, bookCategoryId(category))
                case Some(s) => println(s.sourceAsMap("barcode")); s.sourceAsMap("barcode").toString
              }
            )
          case false => Future.value(barcodeFmt(0, bookCategoryId(category)))
        }
      }
    }

    // Updated by kuangyuan 5/25/2017
    def catalogue(_id: String, rfid: String, reference: String, title: String, stackId: String,
                  clc: String, bookIndex: Int, user: Username, solicited_periodical: String, serial_number: Int): Future[UpsertResponse] = {
      db.execute(
        update(_id).in(_index / _type).docAsUpsert(
          "rfid" -> rfid,
          "reference" -> reference,
          "title" -> title,
          "stack" -> stackId,
          "clc" -> clc,
          "book_index" -> bookIndex,
          "datetime" -> Time.now.toString,
          "user" -> user,
          "solicited_periodical" -> solicited_periodical,
          "serial_number" -> serial_number
        )
      ).toTwitterFuture.map(a => (a.status.getStatus, updateJsonRepsonse(a)))
    }

    def updateAvailability(_id: String, isAvailable: Boolean = false, now: String = Time.now.toString) = {
      this.updateDoc(_id, Map("is_available" -> isAvailable, "datetime" -> now))
    }

    def insert( _id: String, item: String) = {
      db.execute(
        update(_id) in (_index / _type) script {
          script(s"""if (ctx._source.containsKey(params.field)) {params.value.removeAll(ctx._source.items);ctx._source.items.addAll(params.value)} else { ctx._source.items=params.value;}""")
            .lang("painless").params(Map("field" -> "items", "value" -> seqAsJavaList(Seq(item))))
        }
      )
    }

    def delete(_id: String, item: String) = {
      db.execute({
        update(_id) in (_index / _type) script {
          script(s""" ctx._source.items.removeIf(item -> item == \"$item\") """).lang("painless")
        }
      })
    }

    //leixx,2017-4-7
    def findByBarcode(barcode:String):Future[Option[(String, Book)]]={
      db.execute(indexExists("book")).toTwitterFuture.flatMap {
        case a => a.isExists match {
          case true =>
            db.execute(
              search(_index / _type) termQuery("barcode.keyword", barcode)  limit 1
            ).toTwitterFuture.map(a =>
              a.hits.headOption match {
                case None => None
                case Some(s) => Some(s.id,Json.parse(s.sourceAsString).as[Book] )
              }
            )
          case false => Future.value(None)
        }
      }
    }

    // add by yuan kuang 4/12/2017
    def getStackStockData(): Future[SearchResponse] = {
      db.execute(
        search(_index / _type) fetchSource(false) aggregations (
          termsAggregation("stock_category").field("category.keyword")
          )
      ).toTwitterFuture.map(a =>
        searchTermsAggResponse(a, "stock_category", "category_stock", "name", "stock")
      )
    }


    def updateActive(_id: String, isActive: Boolean = false, now: String = Time.now.toString): Future[UpdateResponse] = {
      this.updateDoc(_id, Map("is_active" -> isActive, "datetime" -> now))
    }

    // Added by kuangyuan 4/17/2017
    def findWithReferenceByBarcodeStr(barcode_string : String): Future[(StatusCode, Docs)] = {
      db.execute(indexExists(_index)).toTwitterFuture.flatMap {
        case a => a.isExists match {
          case true =>
            db.execute(
              search(_index / _type) query(termsQuery("barcode.keyword", barcode_string.split(",").toSeq)) limit 10
            ).toTwitterFuture flatMap {
              case a if a.hits.headOption == None => Future.value((404, valueJson("error", "item not found for any of the barcodes")))
              case b =>
                db.execute(indexExists("reference")).toTwitterFuture.flatMap {
                  case re => re.isExists match {
                    case true =>
                      Future.collect(b.hits.map(s =>
                      db.execute(
                        search("reference" / _type) termQuery("_id", Json.parse(s.sourceAsString).as[Book].reference) limit 1
                      ).toTwitterFuture.map(a =>
                        a.hits.headOption match {
                          case None => println("reference not found");
                            s"""
                               |{"item_info": ${s.sourceAsString}, "reference_info": {"value": "empty"} }
                               |""".stripMargin
                          case Some(s1) =>
                            s"""
                               |{"item_info": ${s.sourceAsString}, "reference_info": ${s1.sourceAsString} }
                               |""".stripMargin
                        }
                      ))) flatMap {
                        case all =>
                          val noFinds = barcode_string.split(",").toSeq.filterNot(b.ids.contains(_)).map(no =>
                            s"""
                               |{"item_info": {"barcode": "${no}"}, "reference_info": {"value": "empty"} }
                             """.stripMargin
                          )
                          Future.value((200, valueJsonArrayObject("result", s"[${all.++:(noFinds).mkString(",")}]")))
                      }
                    case false =>
                      Future((502, "reference index does not exist"))
                  }
                }

            }
          case false => Future((502, "book index does not exist"))
        }
//
      }
    }

    // Added by kuangyuan 5/1/2017
    def findActiveBooksUnderStackId(stackId: String): Future[(StatusCode, Docs)] = {
      db.execute(
        search(_index / _type) query (
          boolQuery().must(Seq(termQuery("stack", stackId),termQuery("is_active", true)))
        ) fetchSource false start 0 limit elasticSearchLimit
      ).toTwitterFuture.map(a => a.hits.headOption match {
        case Some(s) => (200, valueJsonArrayObject("book_ids", "[" + a.ids.map(id => '"' + id + '"').mkString(",") + "]"))
        case None => (404, valueJson("result", "Not Found"))
      })
    }

    // Added by kuangyuan 5/8/2017
    def findByRefId(referenceId: String): Future[(StatusCode, Docs)] = {
      db.execute(
        search(_index / _type) query (
          matchPhraseQuery("reference", referenceId)
        ) start 0 limit 1
      ).toTwitterFuture.map(a => a.hits.headOption match {
        case Some(s) => (200, s.sourceAsString)
        case None => (404, valueJson("result", "Not Found"))
      })
    }

    // Added by kuangyuan 5/15/2017
    def findOldBooks(milestoneDate: String): Future[Seq[String]] = {
      db.execute(
        search(_index / _type) query {
          rangeQuery("datetime").lte(milestoneDate)
        } fetchSource false start 0 limit elasticSearchLimit
      ).toTwitterFuture.map(a => a.ids.distinct)
    }

    // Adde by kuangyuan 5/18/2017
    def createBranchBookIndex(name: String, shards_param: Int, replicas_param: Int): Future[CreateIndexResponse] = {
      db.execute(
        createIndex(name) shards shards_param replicas replicas_param mappings(
          mapping("info") as (
            keywordField("id").typed(FieldType.LongType),
            textField("act_shelf").fields(
              keywordField("keyword").ignoreAbove(256)
            ),
            textField("barcode").fields(
              keywordField("book_index").typed(FieldType.LongType)
            ),
            textField("category").fields(
              keywordField("keyword").ignoreAbove(256)
            ),
            textField("clc").fields(
              keywordField("keyword").ignoreAbove(256)
            ),
            dateField("create_at"),
            dateField("datetime"),
            textField("description").fields(
              keywordField("keyword").ignoreAbove(256)
            ),
            longField("id"),
            booleanField("is_active"),
            booleanField("is_available"),
            textField("last_check_id").fields(
              keywordField("keyword").ignoreAbove(256)
            ),
            dateField("last_check_time"),
            textField("reference").fields(
              keywordField("keyword").ignoreAbove(256)
            ),
            textField("rfid").fields(
              keywordField("keyword").ignoreAbove(256)
            ),
            textField("shelf").fields(
              keywordField("keyword").ignoreAbove(256)
            ),
            textField("solicited_periodical").fields(
              keywordField("keyword").ignoreAbove(256)
            ),
            textField("stack").fields(
              keywordField("keyword").ignoreAbove(256)
            ),
            longField("stack_id"),
            textField("title").fields(
              keywordField("keyword").ignoreAbove(256)
            ),
            dateField("update_at"),
            textField("user").fields(
              keywordField("keyword").ignoreAbove(256)
            ),
            longField("user_id")
          )
        )
      ).toTwitterFuture
    }
  }
}
