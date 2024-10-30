// AJAX обновления списка ордеров
document.addEventListener('DOMContentLoaded', function() {
        function loadOrders() {

                let ordersTable = document.getElementById('ordersTable');
                if (!ordersTable) {
                    console.warn("Элемент 'ordersTable' не найден!");
                    return;
                }
            fetch('/order/get-orders')
                .then(response => response.json())
                .then(data => {
                    let ordersTable = document.getElementById('ordersTable');

                    ordersTable.innerHTML = ''; // Очистка таблицы перед добавлением новых данных

                    if (data.length > 0) {
                        data.forEach(order => {
                            let row = document.createElement('tr');
                            // Создание элемента <span> с классом из order.status.color
                            let statusSpan = `<span class="badge ${order.statusColor}">${order.statusName}</span>`;

                            row.innerHTML = `
                                <td>${order.createDate}</td>
                                <td>${order.sum}</td>
                                <td>${statusSpan}</td>
                                <td><a href="#" data-bs-toggle="modal" data-order-id=${order.id} data-bs-target="#viewDetails">Детали</a></td>
                            `;
                            ordersTable.appendChild(row);
                        });
                    } else {
                        ordersTable.innerHTML = '<tr><td colspan="3">Нет заказов для отображения.</td></tr>';
                    }
                })
                .catch(error => console.error('Ошибка при загрузке заказов:', error));
        }

       function loadActions() {

            let actionsTable = document.getElementById('actionsTable');
            if (!actionsTable) {
                console.warn("Элемент 'actionsTable' не найден!");
                return;
            }
            fetch('/order/get-actions')
                .then(response => response.json())
                .then(data => {

                    actionsTable.innerHTML = ''; // Очистка таблицы перед добавлением новых данных

                    if (data.length > 0) {
                        data.forEach(order => {
                            let row = document.createElement('tr');
                            // Создание элемента <span> с классом из order.status.color
                            let statusSpan = `<span class="badge ${order.statusColor}">${order.statusName}</span>`;

                            row.innerHTML = `
                                <td>${order.createDate}</td>
                                <td>${statusSpan}</td>
                                <!--- <td>Счёт №${order.invoiceNumber} от ${order.invoiceDate}</td>
                                <td>${order.productName}</td>
                                <td>${order.productQuantity}</td> --->
                                <td><a href="#" data-bs-toggle="modal" data-order-id=${order.id} data-bs-target="#viewDetails">Детали</a></td>
                            `;
                            actionsTable.appendChild(row);
                        });
                    } else {
                        actionsTable.innerHTML = '<tr><td colspan="3">Нет заказов для отображения.</td></tr>';
                    }
                })
                .catch(error => console.error('Ошибка при загрузке заказов:', error));
       }

       // Get Actions Data
       function loadOrderById() {

           if (event.target.tagName === 'A' && event.target.hasAttribute('data-order-id')) {
               let orderId = event.target.getAttribute('data-order-id');
               console.log("Order ID:", orderId);

                const request = "/order/get-order?id=" + orderId;

               fetch(request)
                   .then(response => response.json())
                   .then(order => {

                   // Находим контейнер внутри модального окна, куда будем вставлять данные
                    let modalBody = document.querySelector('#viewDetails .modal-body');

                    // Очищаем старые данные
                    modalBody.innerHTML = '';

                // Если данные заказа есть
                if (order) {
                    let row = document.createElement('dl');
                    let statusSpan = `<span class="badge ${order.statusColor}">${order.statusName}</span>`;

                    // Формируем HTML для отображения данных
                    row.innerHTML = `
                        <dt>Время создания: </dt>
                        <dd>${order.createDate}</dd>
                        <dt>Счёт: </dt>
                        <dd>${order.invoiceNumber} от ${order.invoiceDate}</dd>
                        <dt>Название товара:</dt>
                        <dd>${order.productName}</dd>
                        <dt>Количество товара:</dt>
                        <dd>${order.productQuantity}</dd>
                        <dt>Статус:</dt>
                        <dd>${statusSpan}</dd>
                    `;

                    // Вставляем данные в модальное окно
                    modalBody.appendChild(row);
                } else {
                    modalBody.innerHTML = '<p>Заказ не найден.</p>';
                }
               })
                .catch(error => console.error('Ошибка при загрузке заказов:', error));

           }
       }

        // Загружаем заказы через таймаут, чтобы убедиться, что HTML загружен
        setTimeout(loadOrders, 100); // 100 миллисекунд
        setTimeout(loadActions, 200); // 100 миллисекунд

        // Также загружаем заказы при клике на вкладку "Home"
        const homeTab = document.getElementById('home-tab');
        homeTab.addEventListener('click', loadOrders);

        const actionsTab = document.getElementById('action-tab');
        actionsTab.addEventListener('click', loadActions);

        // Загрузка ордера по клику подробнее
        const actionsTable = document.getElementById('actionsTable');
        actionsTable.addEventListener('click', loadOrderById );

        const ordersTable = document.getElementById('ordersTable');
        ordersTable.addEventListener('click', loadOrderById );

});



