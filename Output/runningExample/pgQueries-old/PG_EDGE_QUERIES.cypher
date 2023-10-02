CREATE (:string { value : "Stanford University" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "Massachusetts Institute of Technology" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "Math 101" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "Computer Science 202" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "Department of Mathematics" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "Department of Computer Science" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "Bob" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "Alice Smith" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "John" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "Marry Donaldson" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "United States of America" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "Web Engineering" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:date { value : "1995-01-01" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#date"  });
CREATE (:gYear { value : "1994" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#gYear"  });
CREATE (:string { value : "05-05" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "450 Serra Mall, Stanford, CA 94305" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "USA" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "77 Massachusetts Avenue" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "Cambridge" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "MA" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "02139" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
CREATE (:string { value : "US" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });


MATCH (s {iri: "http://x.y/Stanford"}), (u {value: "Stanford University"}) 
WITH s, u
CREATE (s)-[:name]->(u);

MATCH (s {iri: "http://x.y/MIT"}), (u {value: "Massachusetts Institute of Technology"}) 
WITH s, u
CREATE (s)-[:name]->(u);

MATCH (s {iri: "http://x.y/Math101"}), (u {value: "Math 101"}) 
WITH s, u
CREATE (s)-[:name]->(u);

MATCH (s {iri: "http://x.y/CompSci202"}), (u {value: "Computer Science 202"}) 
WITH s, u
CREATE (s)-[:name]->(u);

MATCH (s {iri: "http://x.y/MathDept"}), (u {value: "Department of Mathematics"}) 
WITH s, u
CREATE (s)-[:name]->(u);

MATCH (s {iri: "http://x.y/CompSciDept"}), (u {value: "Department of Computer Science"}) 
WITH s, u
CREATE (s)-[:name]->(u);

MATCH (s {iri: "http://x.y/Bob"}), (u {value: "Bob"}) 
WITH s, u
CREATE (s)-[:name]->(u);

MATCH (s {iri: "http://x.y/alice"}), (u {value: "Alice Smith"}) 
WITH s, u
CREATE (s)-[:name]->(u);

MATCH (s {iri: "http://x.y/John"}), (u {value: "John"}) 
WITH s, u
CREATE (s)-[:name]->(u);

MATCH (s {iri: "http://x.y/Marry"}), (u {value: "Marry Donaldson"}) 
WITH s, u
CREATE (s)-[:name]->(u);

MATCH (s {iri: "http://x.y/UsaCountry"}), (u {value: "United States of America"}) 
WITH s, u
CREATE (s)-[:name]->(u);
MATCH (s {iri: "http://x.y/Bob"}), (u {iri: "http://x.y/CompSci202"}) 
WITH s, u
CREATE (s)-[:takesCourse {iri : "http://x.y/takesCourse"}]->(u);

MATCH (s {iri: "http://x.y/Bob"}), (u {value: "Web Engineering"}) 
WITH s, u
CREATE (s)-[:takesCourse]->(u);
MATCH (s {iri: "http://x.y/John"}), (u {iri: "http://x.y/Math101"}) 
WITH s, u
CREATE (s)-[:takesCourse {iri : "http://x.y/takesCourse"}]->(u);
MATCH (s {iri: "http://x.y/John"}), (u {iri: "http://x.y/CompSci202"}) 
WITH s, u
CREATE (s)-[:takesCourse {iri : "http://x.y/takesCourse"}]->(u);
MATCH (s {iri: "http://x.y/Bob"}), (u {iri: "http://x.y/alice"}) 
WITH s, u
CREATE (s)-[:advisedBy {iri : "http://x.y/advisedBy"}]->(u);
MATCH (s {iri: "http://x.y/John"}), (u {iri: "http://x.y/Marry"}) 
WITH s, u
CREATE (s)-[:advisedBy {iri : "http://x.y/advisedBy"}]->(u);
MATCH (s {iri: "http://x.y/Bob"}), (u {iri: "http://x.y/MIT"}) 
WITH s, u
CREATE (s)-[:studiesAt {iri : "http://x.y/studiesAt"}]->(u);
MATCH (s {iri: "http://x.y/John"}), (u {iri: "http://x.y/MIT"}) 
WITH s, u
CREATE (s)-[:studiesAt {iri : "http://x.y/studiesAt"}]->(u);
MATCH (s {iri: "http://x.y/alice"}), (u {iri: "http://x.y/CompSciDept"}) 
WITH s, u
CREATE (s)-[:worksFor {iri : "http://x.y/worksFor"}]->(u);
MATCH (s {iri: "http://x.y/Marry"}), (u {iri: "http://x.y/MathDept"}) 
WITH s, u
CREATE (s)-[:worksFor {iri : "http://x.y/worksFor"}]->(u);
MATCH (s {iri: "http://x.y/alice"}), (u {iri: "http://x.y/MIT"}) 
WITH s, u
CREATE (s)-[:docDegreeFrom {iri : "http://x.y/docDegreeFrom"}]->(u);
MATCH (s {iri: "http://x.y/Marry"}), (u {iri: "http://x.y/Stanford"}) 
WITH s, u
CREATE (s)-[:docDegreeFrom {iri : "http://x.y/docDegreeFrom"}]->(u);
MATCH (s {iri: "http://x.y/alice"}), (u {iri: "http://x.y/CompSci202"}) 
WITH s, u
CREATE (s)-[:teacherOf {iri : "http://x.y/teacherOf"}]->(u);
MATCH (s {iri: "http://x.y/Marry"}), (u {iri: "http://x.y/Math101"}) 
WITH s, u
CREATE (s)-[:teacherOf {iri : "http://x.y/teacherOf"}]->(u);

MATCH (s {iri: "http://x.y/Bob"}), (u {value: "1995-01-01"}) 
WITH s, u
CREATE (s)-[:dob]->(u);

MATCH (s {iri: "http://x.y/John"}), (u {value: "1994"}) 
WITH s, u
CREATE (s)-[:dob]->(u);

MATCH (s {iri: "http://x.y/John"}), (u {value: "05-05"}) 
WITH s, u
CREATE (s)-[:dob]->(u);
MATCH (s {iri: "http://x.y/MathDept"}), (u {iri: "http://x.y/MIT"}) 
WITH s, u
CREATE (s)-[:subOrgOf {iri : "http://x.y/subOrgOf"}]->(u);
MATCH (s {iri: "http://x.y/CompSciDept"}), (u {iri: "http://x.y/MIT"}) 
WITH s, u
CREATE (s)-[:subOrgOf {iri : "http://x.y/subOrgOf"}]->(u);

MATCH (s {iri: "http://x.y/Stanford"}), (u {value: "450 Serra Mall, Stanford, CA 94305"}) 
WITH s, u
CREATE (s)-[:address]->(u);
MATCH (s {iri: "http://x.y/MIT"}), (u {iri: "http://x.y/MitsAddress"}) 
WITH s, u
CREATE (s)-[:address {iri : "http://x.y/address"}]->(u);

MATCH (s {iri: "http://x.y/Stanford"}), (u {value: "USA"}) 
WITH s, u
CREATE (s)-[:country]->(u);
MATCH (s {iri: "http://x.y/MitsAddress"}), (u {iri: "http://x.y/UsaCountry"}) 
WITH s, u
CREATE (s)-[:country {iri : "http://x.y/country"}]->(u);

MATCH (s {iri: "http://x.y/MitsAddress"}), (u {value: "77 Massachusetts Avenue"}) 
WITH s, u
CREATE (s)-[:street]->(u);

MATCH (s {iri: "http://x.y/MitsAddress"}), (u {value: "Cambridge"}) 
WITH s, u
CREATE (s)-[:city]->(u);

MATCH (s {iri: "http://x.y/MitsAddress"}), (u {value: "MA"}) 
WITH s, u
CREATE (s)-[:state]->(u);

MATCH (s {iri: "http://x.y/MitsAddress"}), (u {value: "02139"}) 
WITH s, u
CREATE (s)-[:zip]->(u);

MATCH (s {iri: "http://x.y/UsaCountry"}), (u {value: "US"}) 
WITH s, u
CREATE (s)-[:isoCode]->(u);
MATCH (s {iri: "http://x.y/Math101"}), (u {iri: "http://x.y/MathDept"}) 
WITH s, u
CREATE (s)-[:offeredBy {iri : "http://x.y/offeredBy"}]->(u);
MATCH (s {iri: "http://x.y/CompSci202"}), (u {iri: "http://x.y/CompSciDept"}) 
WITH s, u
CREATE (s)-[:offeredBy {iri : "http://x.y/offeredBy"}]->(u);
