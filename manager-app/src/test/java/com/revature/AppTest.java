package com.revature;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;  
import org.junit.jupiter.api.AfterEach;  
import static org.junit.jupiter.api.Assertions.*; 

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    @Test
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    @Test
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    @Test
    public void testApp()
    {
        assertTrue( true );
    }
}
