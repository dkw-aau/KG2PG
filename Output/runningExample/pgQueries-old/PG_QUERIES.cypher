CREATE (:Person:Professor:Faculty { iri : "http://x.y/Marry"})
CREATE (:UnderGraduateStudent:Person:Student { iri : "http://x.y/Bob"})
CREATE (:University { iri : "http://x.y/MIT"})
CREATE (:Department { iri : "http://x.y/CompSciDept"})
CREATE (:Course:UnderGradCourse { iri : "http://x.y/CompSci202"})
CREATE (:GraduateCourse:Course { iri : "http://x.y/Math101"})
CREATE (:Country { iri : "http://x.y/UsaCountry"})
CREATE (:Person:Lecturer:Professor:Faculty { iri : "http://x.y/alice"})
CREATE (:University { iri : "http://x.y/Stanford"})
CREATE (:Address { iri : "http://x.y/MitsAddress"})
CREATE (:Person:GraduateStudent:Student { iri : "http://x.y/John"})
CREATE (:Department { iri : "http://x.y/MathDept"})



MATCH (s {iri: "http://x.y/Stanford"}) SET s.name = COALESCE(s.name, "Stanford University"), s.iri = COALESCE(s.iri, "http://x.y/name");
MATCH (s {iri: "http://x.y/MIT"}) SET s.name = COALESCE(s.name, "Massachusetts Institute of Technology"), s.iri = COALESCE(s.iri, "http://x.y/name");
MATCH (s {iri: "http://x.y/Math101"}) SET s.name = COALESCE(s.name, "Math 101"), s.iri = COALESCE(s.iri, "http://x.y/name");
MATCH (s {iri: "http://x.y/CompSci202"}) SET s.name = COALESCE(s.name, "Computer Science 202"), s.iri = COALESCE(s.iri, "http://x.y/name");
MATCH (s {iri: "http://x.y/MathDept"}) SET s.name = COALESCE(s.name, "Department of Mathematics"), s.iri = COALESCE(s.iri, "http://x.y/name");
MATCH (s {iri: "http://x.y/CompSciDept"}) SET s.name = COALESCE(s.name, "Department of Computer Science"), s.iri = COALESCE(s.iri, "http://x.y/name");
MATCH (s {iri: "http://x.y/Bob"}) SET s.name = COALESCE(s.name, "Bob"), s.iri = COALESCE(s.iri, "http://x.y/name");
MATCH (s {iri: "http://x.y/alice"}) SET s.name = COALESCE(s.name, "Alice Smith"), s.iri = COALESCE(s.iri, "http://x.y/name");
MATCH (s {iri: "http://x.y/John"}) SET s.name = COALESCE(s.name, "John"), s.iri = COALESCE(s.iri, "http://x.y/name");
MATCH (s {iri: "http://x.y/Marry"}) SET s.name = COALESCE(s.name, "Marry Donaldson"), s.iri = COALESCE(s.iri, "http://x.y/name");
MATCH (s {iri: "http://x.y/UsaCountry"}) SET s.name = COALESCE(s.name, "United States of America"), s.iri = COALESCE(s.iri, "http://x.y/name");
MATCH (s {iri: "http://x.y/Stanford"}) SET s.country = COALESCE(s.country, "USA"), s.iri = COALESCE(s.iri, "http://x.y/country");
MATCH (s {iri: "http://x.y/MitsAddress"}) SET s.street = COALESCE(s.street, "77 Massachusetts Avenue"), s.iri = COALESCE(s.iri, "http://x.y/street");
MATCH (s {iri: "http://x.y/MitsAddress"}) SET s.city = COALESCE(s.city, "Cambridge"), s.iri = COALESCE(s.iri, "http://x.y/city");
MATCH (s {iri: "http://x.y/MitsAddress"}) SET s.state = COALESCE(s.state, "MA"), s.iri = COALESCE(s.iri, "http://x.y/state");
MATCH (s {iri: "http://x.y/MitsAddress"}) SET s.zip = COALESCE(s.zip, "02139"), s.iri = COALESCE(s.iri, "http://x.y/zip");
MATCH (s {iri: "http://x.y/UsaCountry"}) SET s.isoCode = COALESCE(s.isoCode, "US"), s.iri = COALESCE(s.iri, "http://x.y/isoCode");



MATCH (s {iri: "http://x.y/Bob"}), (u {iri: "http://x.y/CompSci202"}) 
WITH s, u
CREATE (s)-[:takesCourse {iri : "http://x.y/takesCourse"}]->(u);
CREATE (:string { value : "Web Engineering" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
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
CREATE (:date { value : "1995-01-01" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#date"  });
MATCH (s {iri: "http://x.y/Bob"}), (u {value: "1995-01-01"}) 
WITH s, u
CREATE (s)-[:dob]->(u);
CREATE (:gYear { value : "1994" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#gYear"  });
MATCH (s {iri: "http://x.y/John"}), (u {value: "1994"}) 
WITH s, u
CREATE (s)-[:dob]->(u);
CREATE (:string { value : "05-05" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
MATCH (s {iri: "http://x.y/John"}), (u {value: "05-05"}) 
WITH s, u
CREATE (s)-[:dob]->(u);
MATCH (s {iri: "http://x.y/MathDept"}), (u {iri: "http://x.y/MIT"}) 
WITH s, u
CREATE (s)-[:subOrgOf {iri : "http://x.y/subOrgOf"}]->(u);
MATCH (s {iri: "http://x.y/CompSciDept"}), (u {iri: "http://x.y/MIT"}) 
WITH s, u
CREATE (s)-[:subOrgOf {iri : "http://x.y/subOrgOf"}]->(u);
CREATE (:string { value : "450 Serra Mall, Stanford, CA 94305" , iri : "" , dataType : "http://www.w3.org/2001/XMLSchema#string"  });
MATCH (s {iri: "http://x.y/Stanford"}), (u {value: "450 Serra Mall, Stanford, CA 94305"}) 
WITH s, u
CREATE (s)-[:address]->(u);
MATCH (s {iri: "http://x.y/MIT"}), (u {iri: "http://x.y/MitsAddress"}) 
WITH s, u
CREATE (s)-[:address {iri : "http://x.y/address"}]->(u);
MATCH (s {iri: "http://x.y/MitsAddress"}), (u {iri: "http://x.y/UsaCountry"}) 
WITH s, u
CREATE (s)-[:country {iri : "http://x.y/country"}]->(u);
MATCH (s {iri: "http://x.y/Math101"}), (u {iri: "http://x.y/MathDept"}) 
WITH s, u
CREATE (s)-[:offeredBy {iri : "http://x.y/offeredBy"}]->(u);
MATCH (s {iri: "http://x.y/CompSci202"}), (u {iri: "http://x.y/CompSciDept"}) 
WITH s, u
CREATE (s)-[:offeredBy {iri : "http://x.y/offeredBy"}]->(u);
