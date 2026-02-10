package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import Service.ServeurService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Serveur;
import model.User;

public class ServeurServlet extends HttpServlet {
    
    // === Bloc des méthodes GET ===
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Vérifier que l'utilisateur est connecté (session)
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/pages/home.jsp");
            return;
        }

        String url = req.getRequestURL().toString();

        // Bloc "/mes_fichiers" - Récupérer la liste des fichiers de l'utilisateur
        if (url.contains("/mes_fichiers")) {
            Serveur serveur = null;
            try {
                // Créer une NOUVELLE connexion pour cette requête
                serveur = new Serveur("127.0.0.1", 2121);
                serveur.connect();

                PrintWriter srvOut = serveur.getOutWriter();
                BufferedReader in = serveur.getInReader();

                // Authentifier l'utilisateur auprès du serveur
                srvOut.println("LOGIN;" + user.getNom() + ";" + user.getPassword());
                srvOut.flush();

                String authResponse = in.readLine();
                if (authResponse == null || !authResponse.startsWith("Welcome")) {
                    req.setAttribute("error", "Authentification échouée. Veuillez vous reconnecter.");
                    req.getRequestDispatcher("/pages/home.jsp").forward(req, res);
                    return;
                }

                // Récupérer la liste des fichiers
                List<String> list = ServeurService.showhandleList(in, srvOut);

                List<String> nom_dossier = ServeurService.showNomDossier(list);
                // int stockage_utiliser = ServeurService.getStockageUtiliser(list);

                if (list == null || list.isEmpty()) {
                    req.setAttribute("message", "Aucun fichier trouvé.");
                } else {
                    // req.setAttribute("stockage", stockage_utiliser);
                    req.setAttribute("list_file", nom_dossier);
                }

                RequestDispatcher dispat = req.getRequestDispatcher("/pages/mes_fichiers.jsp");
                dispat.forward(req, res);

            } catch (IOException e) {
                e.printStackTrace();
                req.setAttribute("error", "Impossible de se connecter au serveur backend. Vérifiez qu'il est démarré sur le port 2121.");
                req.getRequestDispatcher("/pages/mes_fichiers.jsp").forward(req, res);
            } catch (Exception e) {
                e.printStackTrace();
                req.setAttribute("error", "Erreur lors de la récupération des fichiers : " + e.getMessage());
                req.getRequestDispatcher("/pages/mes_fichiers.jsp").forward(req, res);
            } finally {
                // Fermer la connexion après chaque requête
                if (serveur != null) {
                    try {
                        serveur.close();
                    } catch (Exception e) {
                        // Ignorer les erreurs de fermeture
                    }
                }
            }
        }

        if (url.contains("/accueil")) {
            Serveur serveur = null;
            try {
                // Créer une NOUVELLE connexion pour cette requête
                serveur = new Serveur("127.0.0.1", 2121);
                serveur.connect();

                PrintWriter srvOut = serveur.getOutWriter();
                BufferedReader in = serveur.getInReader();

                // Authentifier l'utilisateur auprès du serveur
                srvOut.println("LOGIN;" + user.getNom() + ";" + user.getPassword());
                srvOut.flush();

                String authResponse = in.readLine();
                if (authResponse == null || !authResponse.startsWith("Welcome")) {
                    req.setAttribute("error", "Authentification échouée. Veuillez vous reconnecter.");
                    req.getRequestDispatcher("/pages/home.jsp").forward(req, res);
                    return;
                }

                // Récupérer la liste des fichiers
                List<String> list = ServeurService.showhandleListUsers(in, srvOut);

                if (list == null || list.isEmpty()) {
                    req.setAttribute("message", "Aucun fichier trouvé.");
                } else {
                    req.setAttribute("list_users", list);
                }

                RequestDispatcher dispat = req.getRequestDispatcher("/pages/list_users.jsp");
                dispat.forward(req, res);

            } catch (IOException e) {
                e.printStackTrace();
                req.setAttribute("error", "Impossible de se connecter au serveur backend. Vérifiez qu'il est démarré sur le port 2121.");
                req.getRequestDispatcher("/pages/accueil.jsp").forward(req, res);
            } catch (Exception e) {
                e.printStackTrace();
                req.setAttribute("error", "Erreur lors de la récupération des fichiers : " + e.getMessage());
                req.getRequestDispatcher("/pages/accueil.jsp").forward(req, res);
            } finally {
                // Fermer la connexion après chaque requête
                if (serveur != null) {
                    try {
                        serveur.close();
                    } catch (Exception e) {
                        // Ignorer les erreurs de fermeture
                    }
                }
            }
        }

        if(url.contains("/stockage")) {
            Serveur serveur = null;
            try {
                // Créer une NOUVELLE connexion pour cette requête
                serveur = new Serveur("127.0.0.1", 2121);
                serveur.connect();

                PrintWriter srvOut = serveur.getOutWriter();
                BufferedReader in = serveur.getInReader();

                // Authentifier l'utilisateur auprès du serveur
                srvOut.println("LOGIN;" + user.getNom() + ";" + user.getPassword());
                srvOut.flush();

                String authResponse = in.readLine();
                if (authResponse == null || !authResponse.startsWith("Welcome")) {
                    req.setAttribute("error", "Authentification échouée. Veuillez vous reconnecter.");
                    req.getRequestDispatcher("/pages/home.jsp").forward(req, res);
                    return;
                }

                // Récupérer le quota de l'utilisateur
                long quota = ServeurService.getQuotaUser(in, srvOut);

                // Récupérer la liste des fichiers pour calculer l'espace utilisé
                List<String> list = ServeurService.showhandleList(in, srvOut);
                long stockageUtilise = ServeurService.getStockageUtiliser(list);

                // Calculer le pourcentage d'utilisation
                int pourcentage = ServeurService.getStoragePercentage(stockageUtilise, quota);

                // Formater les tailles pour l'affichage
                String stockageFormate = ServeurService.formatSize(stockageUtilise);
                String quotaFormate = ServeurService.formatSize(quota);
                long stockageRestant = quota - stockageUtilise;
                String stockageRestantFormate = ServeurService.formatSize(stockageRestant > 0 ? stockageRestant : 0);

                // Envoyer les attributs à la JSP
                req.setAttribute("quota", quota);
                req.setAttribute("stockageUtilise", stockageUtilise);
                req.setAttribute("pourcentage", pourcentage);
                req.setAttribute("stockageFormate", stockageFormate);
                req.setAttribute("quotaFormate", quotaFormate);
                req.setAttribute("stockageRestantFormate", stockageRestantFormate);
                req.setAttribute("nombreFichiers", list != null ? list.size() : 0);

                RequestDispatcher dispat = req.getRequestDispatcher("/pages/stockage.jsp");
                dispat.forward(req, res);

            } catch (IOException e) {
                e.printStackTrace();
                req.setAttribute("error", "Impossible de se connecter au serveur backend.");
                req.getRequestDispatcher("/pages/stockage.jsp").forward(req, res);
            } catch (Exception e) {
                e.printStackTrace();
                req.setAttribute("error", "Erreur lors de la récupération du stockage : " + e.getMessage());
                req.getRequestDispatcher("/pages/stockage.jsp").forward(req, res);
            } finally {
                if (serveur != null) {
                    try {
                        serveur.close();
                    } catch (Exception e) {
                        // Ignorer les erreurs de fermeture
                    }
                }
            }
        }

    }

}
