package com.turing.encripturing;

import android.util.Log;

/**
 * Created by smp_3 on 03/12/2017.
 */

public class Matrix {
    int matrix[][];
    int modulo = 0;

    public Matrix(){}

    public Matrix(int matrix[][], int modulo){
        this.matrix = matrix;
        this.modulo = modulo;
    }

    int [][] get_matrix(){
        return matrix;
    }
    void set_matrix(int matrix[][]){
        this.matrix = matrix;
    }

    public void set_modulo(int modulo){
        this.modulo = modulo;
    }

    int [][] get_inverse(int matrix[][]){
        int h = matrix.length;
        int w = matrix.length;
        int matirx_inverse[][] = new int [h][w] ;
        int det = get_determinant(matrix);
        int inv = modulo_inv(det);
        for(int r=0;r<h;r++){
            for(int c=0;c<w;c++){
                int mul ;
                if((r+c)%2==0)mul = 1;
                else mul=-1;
                matirx_inverse[r][c] = mul * modulo(get_determinant(remove_col_and_row(matrix, r, c)) ) + modulo;
                matirx_inverse[r][c] = modulo(inv * matirx_inverse[r][c]);
            }
        }
        return matirx_inverse;
    }

    int [][] get_inverse(){
        return get_inverse(this.matrix);
    }


    void print_matrix(int matrix[][]){
        for(int i=0;i<matrix.length;i++){
            for(int j=0;j<matrix[0].length;j++){
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    int[][] remove_col_and_row(int matrix[][] , int col, int row){
        int h = matrix.length;
        int w = matrix.length;
        int new_matrix[][] = new int [h-1][w-1];
        int r_id = 0;
        for(int r=0;r<h;r++){
            int c_id = 0;
            if(r == row)continue;
            for(int c=0;c<w;c++){
                if(c == col)continue;
                new_matrix[r_id][c_id] = matrix[r][c];
                c_id++;
            }
            r_id++;
        }
        return new_matrix;
    }

    int modulo(int x){
        if(x < 0)
            return (x%modulo)+modulo;
        else
            return x%modulo;
    }

    int  modulo_inv(int x){
        int num = 1;
        int res = 0;
        while(modulo(res) != 1){
            res = num * x;
            num++;
        }
        return (num-1);
    }


    int get_determinant(int matrix[][]){
        int h = matrix.length;
        int w = matrix[0].length;
        if(h!=w)return -1;//is not a valid matrix;
        int n = h;
        if(n < 1)return -1;//is not a valid matrix;
        if(n==1)return matrix[0][0];
        int ans = 0;

        int new_matrix[][] = new int [n-1][n-1];
        for(int col_to_remove = 0; col_to_remove < n ; col_to_remove++){
            new_matrix = remove_col_and_row(matrix, col_to_remove, 0);
            if(col_to_remove%2 == 0)
                ans += modulo(matrix[0][col_to_remove]) * modulo(get_determinant(new_matrix)) ;
            else
                ans += modulo(-matrix[0][col_to_remove]) * modulo(get_determinant(new_matrix)) ;
            ans = modulo(ans);
        }
        return ans;
    }

    int get_determinant(){
        return get_determinant(this.matrix);
    }

    int [][] multiply(int mat[][]){
        int h = mat.length;
        int w = mat[0].length;
        int ans[][] = new int[h][w];
        for(int r=0;r<h;r++){
            for(int c=0;c<w;c++){
                ans[r][c] = 0;
                for(int k=0;k<w;k++){
                    ans[r][c] +=  modulo(mat[r][k]) * modulo(matrix[k][c]);
                    ans[r][c] = modulo(ans[r][c]);
                }
            }
        }
        return ans;
    }
}
