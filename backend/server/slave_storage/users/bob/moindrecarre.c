#include <stdio.h>

// Prototypes
void readData(char *nomFichier, float x[], float y[], int *n);
void moindrecarre(float x[], float y[], int n, float *a, float *b);
float lagrange(float x[], float y[], int n, float x0);
void displayResultMoindreCarre(float a, float b);
void displayResultLagrange(float y0, float x0);

int main() {
    printf("Methode des moindres carres et Lagrange\n");

    float x[50], y[50];
    int n;
    float x0 = 1;  // point à estimer
    float a = 0.0, b = 0.0;

    // Lecture des données
    readData("data.txt", x, y, &n);

    // Méthode des moindres carrés
    //moindrecarre(x, y, n, &a, &b);
    //displayResultMoindreCarre(a, b);

    // Méthode de Lagrange
    float y0 = lagrange(x, y, n, x0);
    displayResultLagrange(y0, x0);

    return 0;
}

void readData(char *nomFichier, float x[], float y[], int *n) {
    FILE *f = fopen(nomFichier, "r");
    if (!f) {
        printf("Erreur de lecture du fichier\n");
        return;
    }

    fscanf(f, "%d\n", n);
    for (int i = 0; i < *n; i++) {
        fscanf(f, "%f,%f\n", &x[i], &y[i]);
    }

    fclose(f);
}

// Fonction Lagrange corrigée
float lagrange(float x[], float y[], int n, float x0) {
    float Px = 0.0;

    for (int i = 0; i < n; i++) {
        float Pij = 1.0;
        for (int j = 0; j < n; j++) {
            if (i != j) {
                Pij *= (x0 - x[j]) / (x[i] - x[j]);
            }
        }
        Px += y[i] * Pij;
    }

    return Px;
}

void moindrecarre(float x[], float y[], int n, float *a, float *b) {
    float sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

    for (int i = 0; i < n; i++) {
        sumX += x[i];
        sumY += y[i];
        sumXY += x[i] * y[i];
        sumX2 += x[i] * x[i];
    }

    *a = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    *b = (sumY - (*a) * sumX) / n;
}

void displayResultMoindreCarre(float a, float b) {
    printf("\nRésultat par la méthode des moindres carrés\n");
    printf("a = %.6f\n", a);
    printf("b = %.6f\n", b);
    printf("Modèle : y = %.4f x + %.4f\n", a, b);
}

void displayResultLagrange(float y0, float x0) {
    printf("\nRésultat par la méthode de Lagrange\n");
    printf("Pour x = %.4f, y = %.6f\n", x0, y0);
}
