console.log('🔍 Variables de session:');
console.log('  - Course ID:', courseId);
console.log('  - Member ID:', memberId);
console.log('  - User connected:', isUserConnected);
console.log('  - User registered:', isUserRegistered);
console.log('  - User paid:', isUserPaid);

document.addEventListener('DOMContentLoaded', function() {
    lucide.createIcons();
    initializeCourseMap();
    initializeStripePayment();
});

function initializeChat() {
    console.log('💬 Initialisation du chat pour la course:', courseId);

    // Le chat sera géré par le fragment chat.html
    // On peut ajouter ici des événements spécifiques si nécessaire

    // Charger les messages existants
    loadChatMessages();

    // Activer le polling pour les nouveaux messages (toutes les 3 secondes)
    // setInterval(loadChatMessages, 3000);
    console.log('📝 Chat géré par le fragment - pas de polling ici');
}

function loadChatMessages() {
    // Cette fonction sera définie dans le fragment chat
    if (window.chatApp && window.chatApp.loadMessages) {
        console.log('🔄 Appel loadMessages via fragment');
        window.chatApp.loadMessages();
    } else {
        console.log('chatApp non disponible');
    }
}

// Fonction pour accéder au chat (ancre + déplier si nécessaire)
function accessChat() {
    console.log('🔗 Accès au chat demandé');

    const chatContent = document.getElementById('chat-content');
    const toggleBtn = document.getElementById('toggle-chat-content-btn');
    const toggleText = document.getElementById('chat-toggle-text');
    const chevronIcon = toggleBtn?.querySelector('i[data-lucide]');

    // Si le chat est replié, le déplier automatiquement
    if (chatContent && chatContent.style.display === 'none') {
        chatContent.style.display = 'block';
        if (toggleText) toggleText.textContent = 'Réduire';
        if (chevronIcon) chevronIcon.setAttribute('data-lucide', 'chevron-up');

        // Réinitialiser les icônes Lucide
        if (typeof lucide !== 'undefined') {
            lucide.createIcons();
        }

        console.log('Chat déplié automatiquement');
    }

    // Initialiser le chat si pas encore fait
    if (window.chatApp && window.chatApp.init) {
        window.chatApp.init();
    }

    // Faire défiler vers le chat après un petit délai pour l'animation
    setTimeout(() => {
        const chatSection = document.getElementById('chat-section');
        if (chatSection) {
            chatSection.scrollIntoView({
                behavior: 'smooth',
                block: 'start',
                inline: 'nearest'
            });
        }
    }, 150);
}

function initializeCourseMap() {
    // Récupérer les données de la course depuis Thymeleaf
    const course = {
        name: /*[[${course.name}]]*/ "Course par défaut",
        startpositionLatitude: /*[[${course.startpositionLatitude}]]*/ 48.8566,
        startpositionLongitude: /*[[${course.startpositionLongitude}]]*/ 2.3522,
        endpositionLatitude: /*[[${course.endpositionLatitude}]]*/ 48.8606,
        endpositionLongitude: /*[[${course.endpositionLongitude}]]*/ 2.3376
    };

    if (course.startpositionLatitude && course.startpositionLongitude) {
        const map = L.map('map-container').setView([course.startpositionLatitude, course.startpositionLongitude], 13);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(map);

        // Marqueur de départ (teal)
        const startIcon = L.divIcon({
            className: 'custom-marker',
            html: '<div style="background-color: #14b8a6; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 8px rgba(0,0,0,0.3);"></div>',
            iconSize: [24, 24],
            iconAnchor: [12, 12]
        });

        const startMarker = L.marker([course.startpositionLatitude, course.startpositionLongitude], {icon: startIcon})
            .addTo(map)
            .bindPopup(`
                    <div style="font-family: 'Poppins', sans-serif; padding: 12px; max-width: 200px;">
                        <h3 style="font-weight: 600; font-size: 16px; margin-bottom: 8px; color: #14b8a6;">🏁 Point de départ</h3>
                        <p style="font-size: 14px; color: #374151; margin: 0;">${course.name}</p>
                        <p style="font-size: 12px; color: #6b7280; margin-top: 4px;">Début de la course</p>
                    </div>
                `);

        // Marqueur d'arrivée (pink) - seulement si différent du départ
        if (course.endpositionLatitude !== course.startpositionLatitude ||
            course.endpositionLongitude !== course.startpositionLongitude) {

            const endIcon = L.divIcon({
                className: 'custom-marker',
                html: '<div style="background-color: #ec4899; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 8px rgba(0,0,0,0.3);"></div>',
                iconSize: [24, 24],
                iconAnchor: [12, 12]
            });

            const endMarker = L.marker([course.endpositionLatitude, course.endpositionLongitude], {icon: endIcon})
                .addTo(map)
                .bindPopup(`
                        <div style="font-family: 'Poppins', sans-serif; padding: 12px; max-width: 200px;">
                            <h3 style="font-weight: 600; font-size: 16px; margin-bottom: 8px; color: #ec4899;">Point d'arrivée</h3>
                            <p style="font-size: 14px; color: #374151; margin: 0;">${course.name}</p>
                            <p style="font-size: 12px; color: #6b7280; margin-top: 4px;">Fin de la course</p>
                        </div>
                    `);

            // Ajuster la vue pour montrer les deux marqueurs
            const group = new L.featureGroup([startMarker, endMarker]);
            map.fitBounds(group.getBounds().pad(0.1));
        }
    }
}

// Fonction pour gérer le paiement Stripe
function initializeStripePayment() {
    console.log('🔧 Initialisation du paiement Stripe');

    // Bouton paiement Stripe
    const inscriptionBtnStripe = document.getElementById('inscription-btn-stripe');
    // Bouton inscription gratuite
    const inscriptionBtnFree = document.getElementById('inscription-btn-free');

    console.log('Boutons trouvés:', {
        stripe: !!inscriptionBtnStripe,
        free: !!inscriptionBtnFree
    });

    // Gestion inscription gratuite
    if (inscriptionBtnFree) {
        console.log('🆓 Configuration bouton inscription gratuite');
        inscriptionBtnFree.addEventListener('click', function() {
            const courseId = this.getAttribute('data-course-id');
            const courseName = this.getAttribute('data-course-name');

            console.log('Inscription gratuite demandée:', { courseId, courseName });

            // Désactiver le bouton
            this.disabled = true;
            this.innerHTML = '<div class="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full mr-2"></div>Inscription...';

            // Envoyer la demande d'inscription
            fetch('/color_run_war/inscription', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `courseId=${courseId}`
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert(data.message);
                        window.location.reload();
                    } else {
                        alert(data.message);
                        this.disabled = false;
                        this.innerHTML = '<i data-lucide="user-plus" class="w-5 h-5 mr-2"></i>S\'inscrire gratuitement';
                        lucide.createIcons();
                    }
                })
                .catch(error => {
                    console.error('Erreur:', error);
                    alert('Erreur lors de l\'inscription: ' + error.message);
                    this.disabled = false;
                    this.innerHTML = '<i data-lucide="user-plus" class="w-5 h-5 mr-2"></i>S\'inscrire gratuitement';
                    lucide.createIcons();
                });
        });
    }

    // Gestion paiement Stripe
    if (inscriptionBtnStripe) {
        console.log('Configuration bouton paiement Stripe');
        inscriptionBtnStripe.addEventListener('click', function() {
            const courseId = this.getAttribute('data-course-id');
            const courseName = this.getAttribute('data-course-name');
            const price = this.getAttribute('data-price');

            console.log('Inscription avec paiement demandée:', { courseId, courseName, price });

            // Désactiver le bouton pendant le traitement
            this.disabled = true;
            this.innerHTML = '<div class="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full mr-2"></div>Traitement...';

            // Créer la session de paiement
            fetch('/color_run_war/create-checkout-session', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `courseId=${courseId}&courseName=${encodeURIComponent(courseName)}&price=${price}`
            })
                .then(response => {
                    console.log('Réponse du serveur:', response.status);
                    return response.json();
                })
                .then(data => {
                    console.log('Données reçues:', data);
                    if (data.error) {
                        throw new Error(data.error);
                    }

                    // Vérifier qu'on a bien reçu un ID de session
                    if (!data.id) {
                        throw new Error('Aucun ID de session reçu du serveur');
                    }

                    console.log('🔑 ID de session Stripe:', data.id);

                    const stripePublicKey = 'pk_test_51RZVAjPsYYsWDUGNaF1qIxOE8wdvyyaTyHvNEjNk1w0hHZsr2dH8sgHHvFGy8mBMxeRUmxUZjdhwSji8yTAKCa6d00JILg5DZc';
                    const stripe = Stripe(stripePublicKey);

                    console.log('Redirection vers Stripe Checkout...');
                    return stripe.redirectToCheckout({ sessionId: data.id });
                })
                .catch(error => {
                    console.error('Erreur:', error);
                    alert('Erreur lors de la création de la session de paiement: ' + error.message);

                    // Réactiver le bouton
                    this.disabled = false;
                    this.innerHTML = '<i data-lucide="credit-card" class="w-5 h-5 mr-2"></i>Payer et s\'inscrire - ' + price + '€';
                    lucide.createIcons();
                });
        });
    }

    console.log('Initialisation du paiement terminée');
}