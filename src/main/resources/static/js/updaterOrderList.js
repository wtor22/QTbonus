// AJAX обновления списка ордеров
document.addEventListener('DOMContentLoaded', function() {
        function loadOrders() {

                let ordersTable = document.getElementById('ordersTable');
                if (!ordersTable) {
                    console.warn("Элемент 'ordersTable' не найден!");
                    return;
                }
            fetch('/order')
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
                                <td>Счёт №${order.invoiceNumber} от ${order.invoiceDate}</td>
                                <td>${order.productName}</td>
                                <td>${order.productQuantity}</td>
                            `;
                            ordersTable.appendChild(row);
                        });
                    } else {
                        ordersTable.innerHTML = '<tr><td colspan="3">Нет заказов для отображения.</td></tr>';
                    }
                })
                .catch(error => console.error('Ошибка при загрузке заказов:', error));
        }
                // Загружаем заказы через таймаут, чтобы убедиться, что HTML загружен
                setTimeout(loadOrders, 100); // 100 миллисекунд

                // Также загружаем заказы при клике на вкладку "Home"
                const homeTab = document.getElementById('home-tab');
                homeTab.addEventListener('click', loadOrders);

});
