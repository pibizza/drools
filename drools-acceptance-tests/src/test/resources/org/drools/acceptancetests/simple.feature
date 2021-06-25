Feature: Modify
  Use modify to change facts

  Scenario: Drools can modify a fact
    Given the rule
    """
        import org.drools.acceptancetests.model.Person;
        rule R
        when
          	$p : Person( name.length == 5 )
        then
            modify($p) { 
            	setAge($p.getAge()+1) 
            }
        end    
    """
    Given the person "Mario" with age 38
    Given a standard session
    When I fire all the rules
    Then the age of "Mario" is 39
    
    