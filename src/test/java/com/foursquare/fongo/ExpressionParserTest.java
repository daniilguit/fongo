package com.foursquare.fongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;
import static org.junit.Assert.*;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;


public class ExpressionParserTest {

  @Test
  public void testSimpleAndFilter() {
    DBObject query = new BasicDBObject("a", 3).append("n", "j");
    List<DBObject> results = doFilter(
        query,
        new BasicDBObject("a", 3),
        new BasicDBObject("a", Arrays.asList(1,3)).append("n","j"),
        new BasicDBObject("a", 3).append("n", "j"),
        new BasicDBObject("n", "j")
    );
    assertEquals(Arrays.<DBObject>asList(
        new BasicDBObject("a", Arrays.asList(1,3)).append("n", "j"),
        new BasicDBObject("a", 3).append("n", "j")
    ), results);
  }
  
  @Test
  public void testBasicOperators() {
    assertQuery(new BasicDBObject("a", new BasicDBObject("$gte", 4)), Arrays.<DBObject>asList(
        new BasicDBObject("n","stu").append("a", 4),
        new BasicDBObject("n","tim").append("a", 5),
        new BasicDBObject("a", Arrays.asList(3,4))
    ));
    assertQuery(new BasicDBObject("a", new BasicDBObject("$lte", 3)), Arrays.<DBObject>asList(
        new BasicDBObject("n","neil").append("a", 1),
        new BasicDBObject("n","fred").append("a", 2),
        new BasicDBObject("n","ted").append("a", 3),
        new BasicDBObject("a", Arrays.asList(3,4))
    ));
    assertQuery(new BasicDBObject("a", new BasicDBObject("$gt", 4)), Arrays.<DBObject>asList(
        new BasicDBObject("n","tim").append("a", 5)
    ));
    assertQuery(new BasicDBObject("a", new BasicDBObject("$lt", 3)), Arrays.<DBObject>asList(
        new BasicDBObject("n","neil").append("a", 1),
        new BasicDBObject("n","fred").append("a", 2)
    ));
    assertQuery(new BasicDBObject("a", new BasicDBObject("$gt", 3).append("$lt", 5)), Arrays.<DBObject>asList(
        new BasicDBObject("n","stu").append("a", 4),
        new BasicDBObject("a", Arrays.asList(3,4))
    ));

  }
  
  @Test
  public void testNeOperator(){
    DBObject query = new BasicDBObjectBuilder().push("a").add("$ne", 3).pop().get();
    List<DBObject> results = doFilter(
        query,
        new BasicDBObject("a", Arrays.asList(1,3)),
        new BasicDBObject("a", 1),
        new BasicDBObject("a", 3),
        new BasicDBObject("b", 3),
        new BasicDBObject("a", Arrays.asList(1,2))
    );
    assertEquals(Arrays.<DBObject>asList(
        new BasicDBObject("a", 1),
        new BasicDBObject("b", 3),
        new BasicDBObject("a", Arrays.asList(1,2))
    ), results);
  }
  
  @Test
  public void testAllOperator(){
    DBObject query = new BasicDBObjectBuilder().push("a").add("$all", Arrays.asList(2,3)).pop().get();
    List<DBObject> results = doFilter(
        query,
        new BasicDBObject("a", Arrays.asList(2,3)),
        new BasicDBObject("a", null),
        new BasicDBObject("a", Arrays.asList(1,3,4)),
        new BasicDBObject("a", Arrays.asList(1,2,3)),
        new BasicDBObject("a", Arrays.asList(1,3,4))
    );
    assertEquals(Arrays.<DBObject>asList(
        new BasicDBObject("a", Arrays.asList(2,3)),
        new BasicDBObject("a", Arrays.asList(1,2,3))
    ), results);
  }
  
  @Test
  public void tesExistsOperator(){
    DBObject query = new BasicDBObjectBuilder().push("a").add("$exists", true).pop().get();
    List<DBObject> results = doFilter(
        query,
        new BasicDBObject("a", null),
        new BasicDBObject("b", null),
        new BasicDBObject("a", "hi"),
        new BasicDBObject("b", "hi")
    );
    assertEquals(Arrays.<DBObject>asList(
        new BasicDBObject("a", null),
        new BasicDBObject("a", "hi")
    ), results);
  }
  
  @Test
  public void testModOperator(){
    DBObject query = new BasicDBObjectBuilder().push("a").add("$mod", Arrays.asList(10,1)).pop().get();
    List<DBObject> results = doFilter(
        query,
        new BasicDBObject("a", 1),
        new BasicDBObject("a", null),
        new BasicDBObject("a", 21),
        new BasicDBObject("a", 22)
    );
    assertEquals(Arrays.<DBObject>asList(
        new BasicDBObject("a", 1),
        new BasicDBObject("a", 21)
    ), results);
  }
  
  @Test
  public void testInOperator(){
    DBObject query = new BasicDBObjectBuilder().push("a").add("$in", Arrays.asList(2,3)).pop().get();
    List<DBObject> results = doFilter(
        query,
        new BasicDBObject("a", Arrays.asList(1,3)),
        new BasicDBObject("a", 1),
        new BasicDBObject("a", 3)
    );
    assertEquals(Arrays.<DBObject>asList(
        new BasicDBObject("a", Arrays.asList(1,3)),
        new BasicDBObject("a", 3)
    ), results);
  }
  
  @Test
  public void testNinOperator(){
    DBObject query = new BasicDBObjectBuilder().push("a").add("$nin", Arrays.asList(2,3)).pop().get();
    List<DBObject> results = doFilter(
        query,
        new BasicDBObject("a", Arrays.asList(1,4)),
        new BasicDBObject("a", Arrays.asList(1,3)),
        new BasicDBObject("a", 1),
        new BasicDBObject("a", 3)
    );
    assertEquals(Arrays.<DBObject>asList(
        new BasicDBObject("a", Arrays.asList(1,4)),
        new BasicDBObject("a", 1)
    ), results);
  }

  @Test
  public void testNotComplexOperator(){
    DBObject query = new BasicDBObjectBuilder().push("a")
        .push("$not").add("$nin", Arrays.asList(2,3)).pop().pop().get();
    List<DBObject> results = doFilter(
        query,
        new BasicDBObject("a", Arrays.asList(1,4)),
        new BasicDBObject("a", Arrays.asList(1,3)),
        new BasicDBObject("a", 1),
        new BasicDBObject("a", 3)
    );
    assertEquals(Arrays.<DBObject>asList(
        new BasicDBObject("a", Arrays.asList(1,3)),
        new BasicDBObject("a", 3)
    ), results);
    

  }
  
  @Test
  public void testNotSimpleOperator() {
    DBObject query = new BasicDBObjectBuilder().push("a").add("$not", 3).pop().get();
    List<DBObject> results = doFilter(
        query,
        new BasicDBObject("a", Arrays.asList(1,4)),
        new BasicDBObject("a", Arrays.asList(1,3)),
        new BasicDBObject("a", 1),
        new BasicDBObject("a", 3)
    );
    assertEquals(Arrays.<DBObject>asList(
        new BasicDBObject("a", Arrays.asList(1,4)),
        new BasicDBObject("a", 1)
    ), results);
  }
  
  @Test
  public void testEmbeddedMatch(){
    DBObject query = new BasicDBObject("a.b", 1);
    List<DBObject> results = doFilter(
        query,
        new BasicDBObject("a", 1),
        new BasicDBObject("b", 1),
        new BasicDBObject("a",new BasicDBObject("b", 1))
    );
    assertEquals(Arrays.<DBObject>asList(
        new BasicDBObject("a",new BasicDBObject("b", 1))
    ), results);
  }
  
  @Test
  public void testOrOperator(){
    DBObject query = new BasicDBObject("$or", Arrays.asList(
        new BasicDBObject("a",3),
        new BasicDBObject("b", new BasicDBObject("$ne", 3))
    ));
    List<DBObject> results = doFilter(
        query,
        new BasicDBObject("a", 3).append("b", 1),
        new BasicDBObject("a", 1).append("b", 3),
        new BasicDBObject("a", 1).append("b", 1),
        new BasicDBObject("a", 3),
        new BasicDBObject("b", 1),
        new BasicDBObject("a", 5),
        new BasicDBObject("b", 3)
    );
    assertEquals(Arrays.<DBObject>asList(
        new BasicDBObject("a", 3).append("b", 1),
        new BasicDBObject("a", 1).append("b", 1),
        new BasicDBObject("a", 3),
        new BasicDBObject("b", 1),
        new BasicDBObject("a", 5) //i wasn't expected this result, but it works same way in mongo
    ), results);
  }
  
  @Test
  public void testComplexOrOperator(){
    DBObject query = new BasicDBObject("$or", Arrays.asList(
        new BasicDBObject("a",3),
        new BasicDBObject("$or",Arrays.asList( 
            new BasicDBObject("b", 1),
            new BasicDBObject("b", 3)
        ))
    ));
    List<DBObject> results = doFilter(
        query,
        new BasicDBObject("a", 3).append("b", 1),
        new BasicDBObject("a", 1).append("b", 3),
        new BasicDBObject("a", 1).append("b", 7),
        new BasicDBObject("a", 3),
        new BasicDBObject("b", 1),
        new BasicDBObject("a", 5),
        new BasicDBObject("b", 7)
    );
    assertEquals(Arrays.<DBObject>asList(
        new BasicDBObject("a", 3).append("b", 1),
        new BasicDBObject("a", 1).append("b", 3),
        new BasicDBObject("a", 3),
        new BasicDBObject("b", 1)
    ), results);
  }
  
  @Test
  public void testRegexOperator() {
    DBObject query = new BasicDBObject("a", Pattern.compile("^foo"));
    List<DBObject> results = doFilter(
        query,
        new BasicDBObject("a", 1),
        new BasicDBObject("a", null),
        new BasicDBObject("a", "fooSter"),
        new BasicDBObject("a", "funky foo"),
        new BasicDBObject("a", Arrays.asList("foomania", "notfoo"))
    );
    assertEquals(Arrays.<DBObject>asList(
        new BasicDBObject("a", "fooSter"),
        new BasicDBObject("a", Arrays.asList("foomania", "notfoo"))
    ), results);
  }
  
  @Test
  public void testSizeOperator() {
    DBObject query = new BasicDBObjectBuilder().push("a").add("$size", 3).pop().get();
    List<DBObject> results = doFilter(
        query,
        new BasicDBObject("a", null),
        new BasicDBObject("a", Arrays.asList(1,2,3)),
        new BasicDBObject("a", Arrays.asList(1,2,3,4))
    );
    assertEquals(Arrays.<DBObject>asList(
        new BasicDBObject("a", Arrays.asList(1,2,3))
    ), results);
  }

  private void assertQuery(BasicDBObject query, List<DBObject> expected) {
    List<DBObject> results = doFilter(
        query,
        new BasicDBObject("a", null),
        new BasicDBObject("n","neil").append("a", 1),
        new BasicDBObject("n","fred").append("a", 2),
        new BasicDBObject("n","ted").append("a", 3),
        new BasicDBObject("n","stu").append("a", 4),
        new BasicDBObject("n","tim").append("a", 5),
        new BasicDBObject("a", Arrays.asList(3,4))
    );
    assertEquals(expected, results);
  }

  public List<DBObject> doFilter(DBObject ref, BasicDBObject ... input) {
    ExpressionParser ep = new ExpressionParser();
    Filter filter = ep.buildFilter(ref);
    List<DBObject> results = new ArrayList<DBObject>();
    for (DBObject dbo : input) {
      if (filter.apply(dbo)) {
        results.add(dbo);
      }
    }
    return results;
  }

}
