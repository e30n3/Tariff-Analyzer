package org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GenerateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.LoadConfigStatusUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.ValidateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.presentation.base.BaseViewModel
import org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.ConfigurationContract.Action
import org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.ConfigurationContract.Effect
import org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.ConfigurationContract.OperationStatus
import org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.ConfigurationContract.State

class ConfigurationViewModel(
    private val loadConfigStatusUseCase: LoadConfigStatusUseCase,
    private val generateConfigUseCase: GenerateConfigUseCase,
    private val validateConfigUseCase: ValidateConfigUseCase,
) : BaseViewModel<State, Action, Effect>(
    initialState = State(),
    loggerTag = "ConfigurationViewModel",
) {

    init {
        onAction(Action.LoadConfigStatus)
    }

    override fun reduce(action: Action) {
        when (action) {
            Action.LoadConfigStatus -> loadConfigStatus()
            is Action.ChooseTemplatesCsv -> setState {
                it.copy(selectedTemplatesFile = action.file, error = null)
            }

            is Action.ChooseTariffCsv -> setState {
                it.copy(selectedTariffFile = action.file, error = null)
            }

            Action.GenerateConfig -> generateConfig()
            Action.ValidateConfig -> validateConfig()
        }
    }

    private fun loadConfigStatus() {
        setState { it.copy(isLoadingConfigStatus = true, error = null) }
        viewModelScope.launch {
            runCatching { loadConfigStatusUseCase() }
                .onSuccess { result ->
                    setState {
                        it.copy(
                            isLoadingConfigStatus = false,
                            configStatus = result.status,
                            configPath = result.configPath,
                            validationIssues = result.issues,
                        )
                    }
                }
                .onFailure { throwable ->
                    handleFailure(throwable, "Не удалось загрузить статус конфигурации")
                }
        }
    }

    private fun generateConfig() {
        val currentState = state.value
        val templatesFile = currentState.selectedTemplatesFile
        val tariffFile = currentState.selectedTariffFile
        if (templatesFile == null || tariffFile == null) {
            sendEffect(Effect.ShowMessage("Выберите message_templates.csv и tariff.csv"))
            return
        }

        setState { it.copy(operationStatus = OperationStatus.GeneratingConfig, error = null) }
        viewModelScope.launch {
            runCatching { generateConfigUseCase(templatesFile, tariffFile) }
                .onSuccess { result ->
                    setState {
                        it.copy(
                            configStatus = ConfigStatus.Valid,
                            configPath = result.configPath,
                            validationIssues = result.issues,
                            operationStatus = OperationStatus.Idle,
                        )
                    }
                    sendEffect(Effect.ShowMessage("Конфигурация skeleton сгенерирована"))
                }
                .onFailure { throwable ->
                    handleFailure(throwable, "Не удалось сгенерировать конфигурацию")
                }
        }
    }

    private fun validateConfig() {
        if (state.value.configPath == null) {
            sendEffect(Effect.ShowMessage("Сначала сгенерируйте или загрузите конфигурацию"))
            return
        }

        setState { it.copy(operationStatus = OperationStatus.ValidatingConfig, error = null) }
        viewModelScope.launch {
            runCatching { validateConfigUseCase() }
                .onSuccess { issues ->
                    setState {
                        it.copy(
                            configStatus = ConfigStatus.Valid,
                            validationIssues = issues,
                            operationStatus = OperationStatus.Idle,
                        )
                    }
                    sendEffect(Effect.ShowMessage("Skeleton-валидация завершена"))
                }
                .onFailure { throwable ->
                    handleFailure(throwable, "Не удалось проверить конфигурацию")
                }
        }
    }

    private fun handleFailure(throwable: Throwable, fallbackMessage: String) {
        val message = throwable.message ?: fallbackMessage
        logError(throwable, message)
        setState {
            it.copy(
                isLoadingConfigStatus = false,
                operationStatus = OperationStatus.Idle,
                error = message,
            )
        }
        sendEffect(Effect.ShowMessage(message))
    }
}
