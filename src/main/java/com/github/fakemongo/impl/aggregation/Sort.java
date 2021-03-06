package com.github.fakemongo.impl.aggregation;

import com.github.fakemongo.impl.ExpressionParser;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import java.util.List;
import org.bson.util.annotations.ThreadSafe;

/**
 * User: william
 * Date: 24/07/13
 */
@ThreadSafe
public class Sort extends PipelineKeyword {
  public static final Sort INSTANCE = new Sort();

  private Sort() {
  }

  /**
   */
  @Override
  public DBCollection apply(DB originalDB, DBCollection coll, DBObject object) {
    final List<DBObject> objects = coll.find().sort(ExpressionParser.toDbObject(object.get(getKeyword()))).toArray();
    return dropAndInsert(coll, objects);
  }

  @Override
  public String getKeyword() {
    return "$sort";
  }

}
