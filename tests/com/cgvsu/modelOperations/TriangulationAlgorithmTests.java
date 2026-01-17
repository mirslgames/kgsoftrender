package com.cgvsu.modelOperations;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.cgvsu.modelOperations.TriangulationAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TriangulationAlgorithmTests {
    @Test
    public void testTriangulateEven(){

        List<Integer> numbers =  List.of(1,2,3,4,5,6,7,8);
        List<List<Integer>> result = TriangulationAlgorithm.triangulate(numbers);
        List<List<Integer>> real_result = List.of(List.of(1,2,3), List.of(3,4,5), List.of(5,6,7), List.of(7,8,1),
                List.of(1,3,5),List.of(5,7,1));
        assertEquals(real_result, result);
    }
    @Test
    public void testTriangulateOdd(){
        List<Integer> numbers =  List.of(1,2,3,4,5,6,7,8,9);
        List<List<Integer>> result = TriangulationAlgorithm.triangulate(numbers);
        List<List<Integer>> real_result = List.of(List.of(1,2,3), List.of(3,4,5), List.of(5,6,7), List.of(7,8,9),
                List.of(1,3,5),List.of(5,7,9), List.of(1,5,9));
        Assertions.assertEquals(real_result, result);
    }
    @Test
    public void numberOfTrianglesTest(){
        List<Integer> numbers =  List.of(1,2,3,4,5,6,7,8);
        List<Integer> numbers1 =  List.of(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15);
        int n = numbers.size();
        int n1 = numbers1.size();
        List<List<Integer>> result = TriangulationAlgorithm.triangulate(numbers);
        List<List<Integer>> result1 = TriangulationAlgorithm.triangulate(numbers1);
        int n_result = result.size();
        int n_result1 = result1.size();
        assertEquals(n_result, n-2);
        assertEquals(n_result1, n1-2);

    }

}
